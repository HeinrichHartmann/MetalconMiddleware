package de.uniko.west.socialsensor.graphity.server.tomcat;

import java.io.File;
import java.io.IOException;
import java.util.Queue;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import de.uniko.west.socialsensor.graphity.server.Configs;
import de.uniko.west.socialsensor.graphity.server.Server;
import de.uniko.west.socialsensor.graphity.server.exceptions.InvalidUserIdentifierException;
import de.uniko.west.socialsensor.graphity.server.exceptions.RequestFailedException;
import de.uniko.west.socialsensor.graphity.server.exceptions.create.follow.InvalidCreateFollowedIdentifier;
import de.uniko.west.socialsensor.graphity.server.exceptions.create.statusupdate.InvalidStatusUpdateTypeException;
import de.uniko.west.socialsensor.graphity.server.exceptions.create.statusupdate.StatusUpdateInstantiationFailedException;
import de.uniko.west.socialsensor.graphity.server.statusupdates.StatusUpdate;
import de.uniko.west.socialsensor.graphity.server.statusupdates.StatusUpdateManager;
import de.uniko.west.socialsensor.graphity.server.statusupdates.StatusUpdateTemplate;
import de.uniko.west.socialsensor.graphity.server.statusupdates.TemplateFileInfo;
import de.uniko.west.socialsensor.graphity.server.tomcat.create.CreateType;
import de.uniko.west.socialsensor.graphity.server.tomcat.create.FormFile;
import de.uniko.west.socialsensor.graphity.server.tomcat.create.FormItemDoubleUsageException;
import de.uniko.west.socialsensor.graphity.server.tomcat.create.FormItemList;
import de.uniko.west.socialsensor.graphity.socialgraph.NeoUtils;
import de.uniko.west.socialsensor.graphity.socialgraph.operations.ClientResponder;
import de.uniko.west.socialsensor.graphity.socialgraph.operations.CreateFriendship;
import de.uniko.west.socialsensor.graphity.socialgraph.operations.CreateStatusUpdate;
import de.uniko.west.socialsensor.graphity.socialgraph.operations.SocialGraphOperation;

/**
 * Tomcat create operation handler
 * 
 * @author Sebastian Schlicht
 * 
 */
public class Create extends HttpServlet {

	/**
	 * command queue to stack commands created
	 */
	private Queue<SocialGraphOperation> commandQueue;

	/**
	 * server configuration containing file paths
	 */
	private Configs config;

	/**
	 * file item factory
	 */
	private DiskFileItemFactory factory = new DiskFileItemFactory();

	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		final ServletContext context = this.getServletContext();
		final Server server = (Server) context.getAttribute("server");
		this.commandQueue = server.getCommandQueue();
		this.config = server.getConfig();

		// load file factory in temporary directory
		final File tempDir = (File) context
				.getAttribute("javax.servlet.context.tempdir");
		this.factory.setRepository(tempDir);
	}

	@Override
	protected void doPost(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException {
		// store response item for the server response creation
		final ClientResponder responder = new TomcatClientResponder(response);

		if (ServletFileUpload.isMultipartContent(request)) {
			try {
				// read multi-part form items
				final FormItemList items = this.extractFormItems(request);

				long userId;
				try {
					// TODO: OAuth, stop manual determining of user id
					userId = Long.parseLong(items.getField(FormFields.USER_ID));
					if (!NeoUtils.isValidUserIdentifier(userId)) {
						throw new NumberFormatException();
					}
				} catch (final NumberFormatException e) {
					throw new InvalidUserIdentifierException(
							"user identifier has to be greater than zero.");
				}

				// read essential form fields
				final long timestamp = System.currentTimeMillis();
				final CreateType createType = CreateType.GetCreateType(items
						.getField(FormFields.Create.TYPE));

				if (createType == CreateType.FOLLOW) {
					// read followship specific fields
					long followedId;
					try {
						followedId = Long.parseLong(items
								.getField(FormFields.Create.FOLLOW_TARGET));
						if (!NeoUtils.isValidUserIdentifier(followedId)) {
							throw new NumberFormatException();
						}
					} catch (final NumberFormatException e) {
						throw new InvalidCreateFollowedIdentifier(
								"user identifier has to be greater than zero.");
					}

					// create followship
					final CreateFriendship createFriendshipCommand = new CreateFriendship(
							responder, timestamp, userId, followedId);
					this.commandQueue.add(createFriendshipCommand);
				} else {
					// read status update specific fields and files
					final String statusUpdateType = items
							.getField(FormFields.Create.STATUS_UPDATE_TYPE);

					StatusUpdateTemplate template;
					try {
						template = StatusUpdateManager
								.getStatusUpdateTemplate(statusUpdateType);
					} catch (final IllegalArgumentException e) {
						throw new InvalidStatusUpdateTypeException(
								"there is no status update template named \""
										+ statusUpdateType + "\".");
					}

					try {
						this.writeFiles(template, items);

						// create a new status update of the type specified
						final StatusUpdate statusUpdate = StatusUpdateManager
								.instantiateStatusUpdate(statusUpdateType,
										items);

						final CreateStatusUpdate createStatusUpdateCommand = new CreateStatusUpdate(
								responder, timestamp, userId, statusUpdate);
						this.commandQueue.add(createStatusUpdateCommand);
					} catch (final StatusUpdateInstantiationFailedException e) {
						// remove the files
						FormFile fileItem;
						File file;
						for (String fileIdentifier : items.getFileIdentifiers()) {
							fileItem = items.getFile(fileIdentifier);
							file = fileItem.getFile();

							if (file != null) {
								file.delete();
							}
						}

						throw e;
					} catch (final Exception e) {
						throw new IllegalArgumentException(
								"file writing failed!");
					}
				}
			} catch (final FormItemDoubleUsageException e) {
				responder.error(400, e.getMessage());
				e.printStackTrace();
			} catch (final FileUploadException e) {
				responder.error(500,
						"an error encountered while processing the request!");
				e.printStackTrace();
			} catch (final IllegalArgumentException e) {
				// a required form field is missing
				responder.error(500, e.getMessage());
				e.printStackTrace();
			} catch (final RequestFailedException e) {
				// the request contains errors
				responder.addLine(e.getMessage());
				responder.addLine(e.getSalvationDescription());
				responder.finish();
				e.printStackTrace();
			}
		} else {
			// error - no multipart form
			responder
					.error(500, "create requests need to use multipart forms!");
		}
	}

	/**
	 * extract fields and files from the multi-part form in the request
	 * 
	 * @param request
	 *            HTTP servlet request
	 * @return form fields and files wrapped in a form item list
	 * @throws FormItemDoubleUsageException
	 *             if form items use the same identifier
	 * @throws FileUploadException
	 *             if the form item parsing fails
	 */
	private FormItemList extractFormItems(final HttpServletRequest request)
			throws FileUploadException, FormItemDoubleUsageException {
		final ServletFileUpload upload = new ServletFileUpload(this.factory);
		final FormItemList formItems = new FormItemList();

		for (FileItem item : upload.parseRequest(request)) {
			if (item.isFormField()) {
				formItems.addField(item.getFieldName(), item.getString());
			} else {
				formItems.addFile(item.getFieldName(), item);
			}
		}

		return formItems;
	}

	/**
	 * write the files posted if they are matching to the template targeted
	 * 
	 * @param template
	 *            status update template targeted
	 * @param items
	 *            form item list
	 * @throws StatusUpdateInstantiationFailedException
	 *             if a file is not matching the content type set
	 * @throws Exception
	 *             if a file could not be written
	 */
	private void writeFiles(final StatusUpdateTemplate template,
			final FormItemList items) throws Exception {
		FormFile fileItem;
		TemplateFileInfo fileInfo;
		for (String fileIdentifier : items.getFileIdentifiers()) {
			fileItem = items.getFile(fileIdentifier);
			fileInfo = template.getFiles().get(fileIdentifier);

			if (fileInfo.getContentType().equals(fileItem.getContentType())) {
				// write the file and store it within the instantiation item
				final File file = this.writeFile(fileItem);
				fileItem.setFile(file);
			} else {
				throw new StatusUpdateInstantiationFailedException("file \""
						+ fileIdentifier + "\" must have content type "
						+ fileInfo.getContentType());
			}
		}
	}

	/**
	 * write a file to the directory matching its content type
	 * 
	 * @param fileItem
	 *            form file
	 * @return instance of the file written
	 * @throws Exception
	 *             if the file could not be written
	 */
	private File writeFile(final FormFile fileItem) throws Exception {
		final String directory = this.getFileDir(fileItem.getContentType());

		// TODO: ensure unique file names
		final File file = new File(directory + System.currentTimeMillis() + "-"
				+ fileItem.getOriginalFileName());
		fileItem.getFormItem().write(file);
		return file;
	}

	/**
	 * get the directory for a file of the content type specified
	 * 
	 * @param contentType
	 *            file item content type
	 * @return directory for this content type
	 */
	private String getFileDir(final String contentType) {
		return this.config.picture_path;
	}
}