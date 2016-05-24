'use strict';

angular.module('appDynApp', ['ui.router', 'ngSanitize', 'ui.bootstrap', 'ngTagsInput', 'angularTreeview', 'dynform', 'ui.grid', 'infinite-scroll', 'ngFileUpload', 'ngLoadingSpinner', 'ngResource'])

	.config(['$stateProvider','$urlRouterProvider',
	function($stateProvider, $urlRouterProvider) {
	
		$urlRouterProvider.otherwise('/cs');
		$urlRouterProvider.when('/cs', '/cs/listAttributes');
		
        $stateProvider
            
			.state('cs', {
				url:'/cs',
				controller:'masterDataCtrl as mdc',
				templateUrl:'mainmenu/cs.html'                            
            })
			.state('cs.listAttributes', {
				url: '/listAttributes?app_name&formid&pageno',
				controller:'listAttributesCtrl',
				templateUrl: 'views/navigation/cs-listAttributes.html'
			})
			.state('cs.listAuth', {
				url: '/listAuth?app_name&pageno',
				controller:'listAuthCtrl',
				templateUrl: 'search/cs-listAuth.html'
			})
			.state('cs.renderAttributes', {
				url: '/renderAttributes?app_name&formid&dataid',
				controller:'renderAttributesCtrl',
				templateUrl: 'views/navigation/cs-renderAttributes.html'
			})
			.state('cs.listPosts', {
				url:'/listPosts',
				controller:'postShowCtrl',
				templateUrl:'resources/cs-postShow.html'                            
            })
    }]);
