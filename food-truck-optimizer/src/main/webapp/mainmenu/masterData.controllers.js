'use strict';

angular.module('appDynApp')

    .controller('masterDataCtrl', ['$http', '$rootScope','$timeout', 'MasterDataFac',
			function($http, $rootScope, $timeout, MasterDataFac) {

		this.applicationName = 'auth_admin';
		this.applicationDisplayName = '';
		
		var vm = this;
	    MasterDataFac.getApplicationDisplayName(this.applicationName).success(function (data, status, headers, config) {
            vm.applicationDisplayName = data;
        })
        .error(function (data, status, headers, config) {
             vm.status.message="Can't retrieve messages list!";
			 vm.$emit('updateErrorStatus',vm.status);
        });

        this.menuEntries = [];
        MasterDataFac.getFormList(this.applicationName).success(function (data, status, headers, config) {
            vm.menuEntries = data;
        }).error(function (data, status, headers, config) {
             vm.status.message="Can't retrieve messages list!";
			 vm.$emit('updateErrorStatus',vm.status);
        });
    }]);
	