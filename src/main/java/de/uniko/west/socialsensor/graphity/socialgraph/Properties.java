package de.uniko.west.socialsensor.graphity.socialgraph;

/**
 * node property identifiers
 * 
 * @author Sebastian Schlicht
 * 
 */
public final class Properties {

	/**
	 * status update node property identifiers
	 * 
	 * @author Sebastian Schlicht
	 * 
	 */
	public static final class Templates {

		/**
		 * template identifier
		 */
		public static final String IDENTIFIER = "identifier";

		/**
		 * template version
		 */
		public static final String VERSION = "version";

		/**
		 * template's java code
		 */
		public static final String CODE = "code";

		/**
		 * template item name
		 */
		public static final String ITEM_NAME = "name";

		/**
		 * template field type
		 */
		public static final String FIELD_TYPE = "type";

		/**
		 * template file content type
		 */
		public static final String FILE_CONTENT_TYPE = "contentType";

	}

	/**
	 * user node property identifiers
	 * 
	 * @author sebschlicht
	 * 
	 */
	public static final class User {

		/**
		 * user display name
		 */
		public static final String DISPLAY_NAME = "displayName";

		/**
		 * time stamp in milliseconds for the last recent status update
		 */
		public static final String LAST_UPDATE = "last_update";

	}

	/**
	 * status update node property identifiers
	 * 
	 * @author sebschlicht
	 * 
	 */
	public static final class StatusUpdate {

		/**
		 * time stamp in milliseconds of the creation
		 */
		public static final String TIMESTAMP = "timestamp";

		/**
		 * content type identifier (template name)
		 */
		public static final String CONTENT_TYPE = "content_type";

		/**
		 * content object (JSON object)
		 */
		public static final String CONTENT = "content";

	}

}