(function(){
	angular.module('appDynApp')
		.factory('MasterDataFac', ['$http', '$rootScope','$timeout', MasterDataFac]);
	
	function MasterDataFac($http, $rootScope, $timeout) {
    	return {
    		getApplicationDisplayName: getApplicationDisplayName,
    		getFormList: getFormList
    	};
    	
    	function getApplicationDisplayName(applicationName){
            return $http.get('./getApplicationDisplayName/' + applicationName);
    	}
    	
    	function getFormList(applicationName){
    		return $http.get('./getFormList/' + applicationName);
    	}
    };
})();
