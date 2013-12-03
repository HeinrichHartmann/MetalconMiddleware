$(window).load(function() {
	var documentStructure = "<div class=\"form-group message-input\"><label for=\"message\">Message:</label><textarea id=\"message\" class=\"form-control message-input\" rows=\"2\" name=\"message\" type=\"text\" placeholder=\"Enter your Message\"></textarea><div class=\"textbox_buttons-holder\"><input class=\"btn btn-primary btn-sm message-submit\" type=\"submit\" value=\"create status update\" /><button class=\"btn btn-default btn-sm button-clear-link\"> Clear Link</button><button class=\"btn btn-default btn-sm button-clear\"> Clear Text</button><button class=\"btn btn-default btn-sm button-cancel\"> Cancel </button></div></div><div class=\"liveurl\"><div class=\"close\" title=\"Entfernen\"></div><div class=\"inner\"><div class=\"image\"> </div><div class=\"details\"><div class=\"info\"><div class=\"title\"> </div><div class=\"description\"></div><div class=\"url\"> </div></div><div class=\"thumbnail\"><div class=\"pictures\"><div class=\"controls\"><div class=\"prev button inactive\"></div><div class=\"next button inactive\"></div><div class=\"count\"><span class=\"current\">0</span><span> von </span><span class=\"max\">0</span></div></div></div></div><div class=\"video\"></div></div></div></div>";
	$(documentStructure).prependTo('.wrapper');

	$(".btn.btn-primary.message-submit").click(function(){
		var message = document.getElementById("message").value;
		if(message) {
			var fd = new FormData();
			var link_box_string;
			if($(".description").html()){	
				var link_title = "<a href=\""+$(".url").html()+"\">"+$(".title").html()+"</a>"; 
				var link_desc = $(".description").html();  
				if($(".video").html()) {
					var link_vid = $(".video").html();
					link_box_string +="<div class=\"link_box\">" + link_vid + "</div>";
				} else if($(".image .active").attr("src")){
					var link_pic = "<img src=\""+$(".image .active").attr("src")+"\" class=\"img-rounded\">";
					link_box_string = "<div class=\"link_box\"><div class=\"link_picture\">"+link_pic+"</div><div class=\"link_text_wrapper\"><div class=\"link_title\">"+link_title+"</div><div class=\"link_description\">"+link_desc+"</div></div></div>";
				} else link_box_string = "<div class=\"link_box\"><div class=\"link_text_wrapper\"><div class=\"link_title\">"+link_title+"</div><div class=\"link_description\">"+link_desc+"</div></div></div>";
				message +=link_box_string;
			}  
			var type = "status_update";
			var userid = renderer.user;
			var displayName = user_name; 
			var localDate = renderer.format(new Date());
			var rand = "AB" + Math.round(Math.random() * 1000000);
			message = message.replace(/\n/g, '<br />');

			fd.append('type' ,type);
			fd.append('user_id' ,userid);
			fd.append('status_update_id' ,rand);
			fd.append('message' ,message);
			fd.append('status_update_type',"Plain");
			$.ajax({
			  url: 'http://localhost:8080/Graphity-Server-0.1/create',
			  //url: 'http://192.168.178.36:8080/Graphity-Server-0.1/create',
			  data: fd,
			  processData: false,
			  cache: false,
			  contentType: false,
			  processData: false,
			  type: 'POST',
			  success: function(data){
			    if(data.statusMessage ==="ok"){
			    	renderForm(rand, localDate, userid, message, displayName);
			    } //End if
			  }, //End success function
			  error:function(){
			  	renderForm("1","2","3","4");
			  }
			}); //End AJAX
			function renderForm(id_link, date,id_actor, message, actorName){
				//var stringOfValues = "<li><strong><a href=\""+id_link+"\">"+date+"</a></strong><br><span>"+id_actor+": "+message+"</span></li>";
				var stringOfValues = "<li id=\""+00+"\" class=\"single-comment-holder\"><div class=\"user-img\"><a href=\""+id_link+"\"><img src=\"http://www.metalcon.de/images/metal-community.jpg\" class=\"user-img-pic\"></a></div><div class=\"comment-body\"><h3 class=\"username-field\">"+actorName+"</h3><div class=\"comment-date\"><a href=\""+id_link+"\">"+date+"</a></div><div class=\"comment-text\">"+message+"</div></div></li>";
				$(stringOfValues).prependTo('.comments-holder').hide().slideDown("slow");
			} //End renderForm function
		} //End if	
	}); //End click function
	textboxBehavior();
}); //End ready function