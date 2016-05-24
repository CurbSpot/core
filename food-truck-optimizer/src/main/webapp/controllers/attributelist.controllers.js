'use strict';

angular.module('appDynApp')

    .controller('listAttributesCtrl', ['$scope', '$http', '$location','$window', function($scope, $http, $location,$window) {

        $scope.formName = '';
        $scope.messages = [];
    	$scope.status={};
		$scope.status.message="";
		
		if($scope.formid != $location.search().formid){
			$scope.pageno = 0;
		}

		if($location.search().formid != undefined){	
			$scope.formid = $location.search().formid;
		} 
		if($location.search().app_name != undefined){
			$scope.app_name = $location.search().app_name;
		}
		if($location.search().pageno != undefined){
			$scope.pageno = $location.search().pageno;
		}

        $http.get('./getFormDataList/' + $scope.app_name + '/' + $scope.formid + '/' + $scope.pageno).success(function (data, status, headers, config) {
        	$scope.formName = data.formName;
        	$scope.messages = data.formList;
        	$scope.pagesAvailable = data.pagesAvailable;
        }).error(function (data, status, headers, config) {
             $scope.status.message="Can't retrieve messages list!";
			 $scope.$emit('updateErrorStatus',$scope.status);
        });

        $scope.loadMore = function() {
        };

        $scope.deleteAttribute = function(id) {           
        	$http.post('./deleteRecord/' + $scope.app_name + '/' + id +'/' + $location.search().formid)
        	.success(function (data, status, headers, config) {
            	$scope.status.message="Deleted Successfully";
 				$scope.$emit('updateSaveStatus',$scope.status);
                $scope.messages.formList = $scope.messages.formList.filter(function(message) {
                        return message.id != id;
                    }
                );
        		        		
            }).error(function (data, status, headers, config) {
            	 $scope.status.message="Error occurred";
				 $scope.$emit('updateErrorStatus',$scope.status);
            });
        };
        
        /*$scope.GenerateSchema = function(id) {
            $http.post('./generateJsonSchema/' + $scope.app_name + '/' + id).success(function (data, status, headers, config) {
            }).error(function (data, status, headers, config) {
            	 $scope.status.message="Error occurred";
				 $scope.$emit('updateErrorStatus',$scope.status);
            });
        };*/
    }])
	