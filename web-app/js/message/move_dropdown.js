$(document).ready(function() {
	$("#message-actions").change(moveAction);
});

function moveAction() {
	var count = countCheckedMessages();
	var checkedMessageList = '';
	var messageSection = $('input:hidden[name=messageSection]').val();
	var ownerId = $('input:hidden[name=ownerId]').val();
	if(messageSection == 'poll' || messageSection == 'folder' || messageSection == 'radioShow'){
		var location = url_root + "message/"+messageSection+"/"+ownerId;
	} else{
		var location = url_root + "message/"+messageSection;
	}
	var me = $(this).find('option:selected');
	if(count > 1) {
		moveMultipleMessages(me, location);
		return;
	}

	var mesId = $("#message-id").val()
	if(me.hasClass('na')) return;
	if(me.hasClass('poll')) {
		var section = 'poll';
	} else if(me.hasClass('folder')) {
		var section = 'folder';
	}
	
	$.ajax({
		type:'POST',
		data: {messageSection: section, messageId: mesId, ownerId: me.val()},
		url: url_root + 'message/move',
		success: function(data) {
			window.location = location;
		}
	});
}

function moveMultipleMessages(object, location) {
	var	checkedMessageIdList = $('input:hidden[name=checkedMessageIdList]').val();
	if(object.hasClass('poll')) {
		var section = 'poll';
	} else if(object.hasClass('folder')) {
		var section = 'folder';
	}
	$.ajax({
		type:'POST',
		data: {messageSection: section, ownerId: object.val(), checkedMessageIdList: checkedMessageIdList},
		url: url_root + 'message/move',
		success: function(data) {
			window.location = location;
		}
	});
}
