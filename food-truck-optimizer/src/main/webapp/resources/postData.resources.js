'use strict';

angular.module('appDynApp')
	.factory("Post", function($resource) {
  		return $resource("/api/posts/:id", { id: '@_id' }, {
		    update: {method: 'PUT'},
		    delete: {method: 'DELETE', params: {id: '@id'}},
		    create: {method: 'POST'}
		});
});
