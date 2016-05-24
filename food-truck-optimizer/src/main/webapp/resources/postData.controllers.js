'use strict';

angular.module('appDynApp')
    .controller('postShowCtrl', function($scope, Post) {
  		Post.query(function(data) {
    		$scope.posts = data;
  		});
  		
  		$scope.createPost = function(){
  			Post.create("Krishna1");
  		}
  		$scope.editPost = function(data){
  			Post.query({ 'id': data });
  			Post.update(data);
  		}
  		$scope.deletePost = function(data){
  			Post.delete({ 'id': data });
  		}
});