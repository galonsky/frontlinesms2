modules = {
	common { dependsOn 'frontlinesms-core' }
	'frontlinesms-core' {
		dependsOn "jquery, jquery-ui"
		resource url:[dir:'css', file:'reset.css']
		resource url:[dir:'css', file:'layout.css']
		resource url:[dir:'css', file:'head.css']
		resource url:[dir:'css', file:'controls.css']
		resource url:[dir:'css', file:'message.css']
		resource url:[dir:'css', file:'contact.css']
		resource url:[dir:'css', file:'archive.css']
		resource url:[dir:'css', file:'activity.css']
		resource url:[dir:'css', file:"activity/webconnection.css"]
		resource url:[dir:'css', file:'search.css']
		resource url:[dir:'css', file:'settings.css']
		resource url:[dir:'css', file:'status.css']
		resource url:[dir:'css', file:'wizard.css']


		resource url:[dir:'js/layout', file:'resizer.js'], disposition:'head'
		resource url:[dir:'css', file:'status.css']

		resource url:[dir:'css', file:'help.css']

		resource url:[dir:'js', file:"frontlinesms_core.js"], disposition:'head'
		resource url:[dir:'js', file:"activity/popups.js"], disposition:'head'
		resource url:[dir:'js', file:"activity/poll/poll_graph.js"], disposition:'head'
		resource url:[dir:'js', file:'button.js'], disposition:'head'
		resource url:[dir:'js', file:'characterSMS-count.js'], disposition:'head'
		resource url:[dir:'js', file:'check_li.js'], disposition:'head'
		resource url:[dir:'js', file:"jquery.ui.selectmenu.js"], disposition:'head'
		resource url:[dir:'js', file:"jquery.validate.min.js"], disposition:'head'
		resource url:[dir:'js', file:"mediumPopup.js"], disposition:'head'
		resource url:[dir:'js', file:"pagination.js"], disposition:'head'
		resource url:[dir:'js', file:"smallPopup.js"], disposition:'head'
		resource url:[dir:'js', file:"status_indicator.js"], disposition:'head'
		resource url:[dir:'js', file:"system_notification.js"], disposition:'head'
		resource url:[dir:'js', file:'magicwand.js'], disposition:'head'
		resource url:[dir:'js', file:'selectmenuTools.js'], disposition:'head'
		resource url:[dir:'js', file:"activity/popupCustomValidation.js"], disposition:'head'
	}
	
	messages {
		dependsOn "jquery, jquery-ui, common"
		resource url:[dir:'js', file:"message/arrow_navigation.js"], disposition:'head'
		resource url:[dir:'js', file:"message/star_message.js"], disposition:'head'
		resource url:[dir:'js', file:"message/categorize_dropdown.js"], disposition:'head'
		resource url:[dir:'js', file:"message/move_dropdown.js"], disposition:'head'
		resource url:[dir:'js', file:"message/moreActions.js"], disposition:'head'
	}
	
	newMessagesCount {
		dependsOn "jquery"
		resource url:[dir:'js', file:"message/check_for_new_messages.js"]
	}
	
	archive {
		dependsOn "messages"
	}
	
	contacts {
		dependsOn "common"
		resource url:[dir:'js', file:"contact/buttonStates.js"]
		resource url:[dir:'js', file:"contact/moreGroupActions.js"]
		resource url:[dir:'js', file:"contact/search_within_list.js"]
		resource url:[dir:'js', file:"contact/show-groups.js"]
		resource url:[dir:'js', file:"contact/show-fields.js"]
		resource url:[dir:'js', file:"contact/validateContact.js"]
	}

	status {
		dependsOn "common"
		resource url:[dir:'js', file:"datepicker.js"]
	}

	graph {
		resource url:[dir:'js', file:'/graph/graph-utils.js']
		resource url:[dir:'js', file:'/graph/jquery.jqplot.min.js']
		resource url:[dir:'js', file:'/graph/jqplot.barRenderer.min.js']
		resource url:[dir:'js', file:'/graph/jqplot.categoryAxisRenderer.min.js']
		resource url:[dir:'js', file:'/graph/jqplot.pointLabels.min.js']
		resource url:[dir:'js', file:'/graph/jqplot.highlighter.min.js']
		resource url:[dir:'js', file:'/graph/jqplot.enhancedLegendRenderer.min.js']
		resource url:[dir:'css', file:"jquery.jqplot.css"]
	}

	search {
		dependsOn "messages"
		resource url:[dir:'js', file:"datepicker.js"]
		resource url:[dir:'js', file:"search/moreOptions.js"]
		resource url:[dir:'js', file:"search/basicFilters.js"]
	}
	
	settings {
		dependsOn "common"
		resource url:[dir:'js', file:'/settings/basicAuthValidation.js']
		resource url:[dir:'js', file:'/settings/connectionTooltips.js']
	}
	
	overrides {
		'jquery-theme' {
			resource id: 'theme', url:[dir:'jquery-ui', file:"themes/medium/jquery-ui-1.8.11.custom.css"]
		}
	}

}
