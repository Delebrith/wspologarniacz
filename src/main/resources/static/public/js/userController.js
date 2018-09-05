app.controller('userController', function($scope, $http, $cookies, $window) {
	function invalidEmail()
	{
		alert('Niepoprawny adres email!');
	}

	function failedLogin(response)
	{
		alert('Nieudane logowanie!');
	}

	function succesfulLogin(response)
	{
		$cookies.put('token', response.data.token)
		$window.location.reload()
	}

	$scope.submit = function()	{
		var userCredentialsDto = {
			email: $scope.email,
			password: $scope.password
		};

		if ($scope.email == undefined) {
			invalidEmail();
			return;
		}

	    var response = $http.post("/user/login", userCredentialsDto);
	    response.then(
	    	function(response) {
				succesfulLogin(response);
	    	},
	    	function(response){
				failedLogin(response);
    		});
	}

	$scope.logout = function() {
		$cookies.remove('token');
		$window.location.href = "/#";
	}
});
