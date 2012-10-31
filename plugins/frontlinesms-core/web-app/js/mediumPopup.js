var mediumPopup = (function() {
	var ___start___,
		cancel, submit, submitWithoutClose, range,
		selectSubscriptionGroup, editConnection, validateSmartGroup, // TODO move these activity/content-specific methods to somewhere more suitable
		createModalBox,
		launchMediumPopup, launchMediumWizard, launchHelpWizard,
		getCurrentTab, getCurrentTabDom, getCurrentTabIndex, getTabLength,
		prevButton, nextButton,
		addValidation, enableTab, disableTab,
		changeButtons, getButtonToTabMappings, initializeTabContentWidgets, makeTabsUnfocusable,
		tabValidates, validateTabSelections, validateAllPreviousTabs,
		messageResponseClick, moveToRelativeTab,
		___end___;
	cancel = function() {
		$(this).dialog('close');
	};
	createModalBox = function(html) {
		var modalBox = $("<div id='modalBox'><div>");
		modalBox.html(html).appendTo(document.body);
		return modalBox;
	};
	getCurrentTab = function() {
		var selected = $("#tabs").tabs("option", "selected");
		return $("#tabs").find('.ui-tabs-panel').eq(selected);
	};
	getCurrentTabDom = function() {
		return $('#tabs').tabs('option', 'selected');
	};
	getCurrentTabIndex = function() {
		var current, tabWidget;
		tabWidget = $('#tabs').tabs();
		current = tabWidget.tabs('option', 'selected');
		return current;
	};
	launchMediumPopup = function(title, html, btnFinishedText, submitAction) {
		var modalBox = createModalBox(html);
		modalBox.dialog({
				modal: true,
				width: 675,
				height: 500,
				title: title,
				buttons: [{ text:i18n("action.cancel"), click:cancel, id:"cancel" }, { text:i18n("action.back"), disabled:"true" },
						{ text:btnFinishedText, click:submitAction, id:"submit" }],
				close: function() { $(this).remove(); }
		});
		addChangeHandlersForRadiosAndCheckboxes();
		initializePopup(modalBox);
	};
	launchMediumWizard = function(title, html, btnFinishedText, width, height) {
		launchMediumWizard(title, html, btnFinishedText, width, height, true);
	};
	launchMediumWizard = function(title, html, btnFinishedText, width, height, closeOnSubmit) {
		var closeWhenDone, modalBox;
		closeWhenDone = (typeof closeOnSubmit === 'undefined'? true: closeOnSubmit);
		modalBox = createModalBox(html);
		$("#messageText").keyup();
		magicwand.init(modalBox.find('select[id^="magicwand-select"]'));
		modalBox.dialog({
			modal: true,
			title: title,
			minWidth: 675,
			minHeight: 500,
			width: width,
			height: height,
			buttons: [
				{ text:i18n("action.cancel"), click: cancel, id:"cancel" },
				{ text:i18n("action.back"), id:"disabledBack", disabled:true },
				{ text:i18n("action.back"), click:prevButton, id:"prevPage" },
				{ text:i18n("action.next"), click:nextButton, id:"nextPage" },
				{ text:btnFinishedText, click:closeWhenDone? submit: submitWithoutClose, id:"submit" }
			],
			close: function() { $(this).remove(); }
		});
		addChangeHandlersForRadiosAndCheckboxes();
		makeTabsUnfocusable();
		validateTabSelections(modalBox);
		changeButtons(getButtonToTabMappings(),  getCurrentTabDom());
		initializeTabContentWidgets();
		initializePopup();
	};
	launchHelpWizard = function(html) {
		var dialog, height, modalBox;
		modalBox = createModalBox(html);
		modalBox.addClass("help");
		$("#messageText").keyup();
		height = $(window).height();
		dialog = modalBox.dialog({
			modal: true,
			title: i18n("popup.help.title"),
			width: "95%",
			height: height,
			buttons: [
				{ text:i18n("action.close"), click:submit, id:"submit" }
			],
			close: function() { $(this).remove(); }
		});
		addChangeHandlersForRadiosAndCheckboxes();
		$(".ui-dialog").addClass("help");
		initializePopup();
	};
	submitWithoutClose = function() {
		$("#submit").attr('disabled', 'disabled');
		if(tabValidates(getCurrentTab())) {
			$(this).find("form").submit();
		} else {
			$("#submit").removeAttr('disabled');
			$('.error-panel').show();
		}
	};
	submit = function() {
		$("#submit").attr('disabled', 'disabled');
		if(tabValidates(getCurrentTab())) {
			$(this).find("form").submit();
			$(this).dialog('close');
		} else {
			$("#submit").removeAttr('disabled');
			$('.error-panel').show();
		}
	};
	prevButton = function() {
		var i, prevTab;
		for (i = 1; i <= getCurrentTabIndex(); i++) {
			prevTab = getCurrentTabIndex() - i;
			if ($.inArray(prevTab, $("#tabs").tabs("option", "disabled")) === -1) {
				$("#tabs").tabs('select', prevTab);
				break;
			}
		}
	};
	nextButton = function() {
		var i, nextTab;
		for (i=1; i<=getTabLength(); ++i) {
			nextTab = getCurrentTabIndex() + i;
			if ($.inArray(nextTab, $("#tabs").tabs("option", "disabled")) === -1) {
				$("#tabs").tabs('select', nextTab);
				break;
			}
		}
	};
	validateTabSelections = function(dialog) {
		dialog.find('#tabs').tabs({select: function(event, ui) {
			if(ui.index > getCurrentTabIndex()) {
				validateAllPreviousTabs(ui.index);
				var thisTabValidates = tabValidates(getCurrentTab());
				if(thisTabValidates) {
					changeButtons(getButtonToTabMappings(), ui.index);
					if(thisTabValidates && $('.error-panel')) {
						$('.error-panel').hide();
					}
					$(ui.panel).find('input', 'textarea', 'textfield').first().focus();
				} else {
					$('.error-panel').show();
				}
				return thisTabValidates;
			} else {
				changeButtons(getButtonToTabMappings(), ui.index);
				return true;
			}
		}});
	};
	tabValidates = function(tab) {
		return tab.contentWidget('validate');
	};
	addValidation = function(tabName, rules) {
		$(".tab-content-" + tabName).contentWidget({
			validate: rules
		});
	};
	validateAllPreviousTabs = function(selectedTabIndex) {
		var i;
		for(i=0; i<selectedTabIndex; ++i) {
			if(!tabValidates($("#tabs").find('.ui-tabs-panel').eq(i))) {
				$('#tabs').tabs('select', i);
				//$('.error-panel').show();
				return false;
			}
		}
	};
	changeButtons = function(buttonToTabMappings, tabIndex) {
		$.each(buttonToTabMappings, function(key, value) {
			if (value.indexOf(tabIndex) !== -1) {
				$(".ui-dialog-buttonpane #" + key).show();
			} else {
				$(".ui-dialog-buttonpane #" + key).hide();
			}
		});
	};
	range = function(first, last) {
		var a, i;
		a = [];
		for (i=first; i<=last; ++i) {
			a.push(i);
		}
		return a;
	};
	makeTabsUnfocusable = function() {
		$("#tabs").find('input', 'textarea', 'textfield').first().focus();
		$('a[href^="#tabs"]').attr('tabindex', '-1');
	};
	getTabLength = function() {
		return $('#tabs').tabs("length") - 1;
	};
	getButtonToTabMappings = function() {
		return {
				"cancel": range(0, getTabLength()),
				"prevPage": range(1, getTabLength()),
				"nextPage": range(0, getTabLength() - 1),
				"submit": [getTabLength()],
				"disabledBack": [0]
		};
	};
	initializeTabContentWidgets = function() {
		var i;
		for(i=0; i <= getTabLength(); i++) {
			$("#tabs-" + (i + 1)).contentWidget();
		}
	};
	disableTab = function(tabName) {
		var tabNumber;
		// TODO change this to tabName
		if(typeof(tabName) === 'number') {
			tabNumber = tabName;
			$('#tabs').tabs("disable", tabNumber);
			$('.tabs-' + (tabNumber + 1)).addClass('disabled-tab');
		} else if(typeof(tabName) === 'string') {
			tabNumber = $('.tab-content-' + tabName).attr("id").substring("tabs-".length);
			$('#tabs').tabs("disable", tabNumber);
			$('.tab-' + tabName).addClass('disabled-tab');
		}
	};
	enableTab = function(tabName) {
		var tabNumber;
		if(typeof(tabName) === 'number') {
			tabNumber = tabName;
			$('#tabs').tabs("enable", tabNumber);
			$('.tabs-' + (tabNumber + 1)).removeClass('disabled-tab');
		} else if(typeof(tabName) === 'string') {
			tabNumber = $('.tab-content-' + tabName).attr("id").substring("tabs-".length);
			$('#tabs').tabs("enable", tabNumber);
			$('.tab-' + tabName).removeClass('disabled-tab');
		}
		
	};
	moveToRelativeTab = function(offset) {
		$('#tabs').tabs('select', getCurrentTabIndex() + offset);
	};
	//> FUNCTION-SPECIFIC METHODS
	messageResponseClick = function(messageType) {
		var configureTabs, checkedMessageCount, me, src, messageSection, text;
		configureTabs= "";
		me = $(this);
		if (messageType === "Reply") {
			configureTabs = "tabs-1, tabs-3, tabs-4";
			checkedMessageCount = getCheckedItemCount("message");
			if(checkedMessageCount > 0) {
				src = getCheckedList("message");
			} else {
				src = $("#message-src").val();
			}
		} else if(messageType === "Forward") {
			text = $("#single-message #message-detail-content p").text().trim();
		}
		messageSection = $("input:hidden[name=messageSection]").val();
		
		$.ajax({
			type:'POST',
			data: {recipients: src, messageText: text, configureTabs: configureTabs},
			url: url_root + 'quickMessage/create',
			success: function(data, textStatus){ launchMediumWizard(messageType, data, i18n('action.send')); }
		});
	};

	editConnection = function(id) {
		$.ajax({
			url: url_root + "connection/wizard/" + id,
			success: function(data){
				launchMediumWizard(i18n('connection.edit'), data, i18n('action.done'), 675, 500, false);
			}
		});
	};

	selectSubscriptionGroup = function(groupId) { // FIXME activity-specific code should not be inside this file
		var labelId = $('input[value=group-'+groupId+']').attr('id');
		$('label[for='+labelId+']').trigger('click');
	};

	return {
		addValidation:addValidation,
		disableTab:disableTab,
		editConnection:editConnection, // TODO move this somewhere more suitable
		enableTab:enableTab,
		launchMediumPopup:launchMediumPopup,
		launchMediumWizard:launchMediumWizard,
		launchHelpWizard:launchHelpWizard,
		messageResponseClick:messageResponseClick, // TODO move this somewhere more suitable
		selectSubscriptionGroup:selectSubscriptionGroup, // TODO move this somewhere more suitable
		submit:submit
	};
}());

$.widget("ui.contentWidget", {
	// TODO couldn't this just read: `validate: this.options.validate`?
	validate:function() { return this.options.validate(); },
	options:{ validate:function() { return true; } } });

