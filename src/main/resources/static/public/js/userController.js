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

	$scope.login = function()	{
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
		$window.location.href = "/";
		$window.reload();
	}

	$scope.requestPasswordReset = function() {
	    if ($scope.email == undefined) {
            invalidEmail();
            return;
        }

        var userCredentialsDto = {
            email: $scope.email
        };

        var response = $http.post("/user/password/reset/request", userCredentialsDto);
        response.then(
            function(response) {
                alert(response);
            },
            function(response){
                alert(response);
            });
	}

    $scope.passwordResetConfirm = function() {
        var token = $scope.params['token']

        var passwordDto = {
            password: $scope.password
        };

        var response = $http.post("/user/password/reset/confirm/" + token, passwordDto);
        response.then(
            function(response) {
                $scope.successMessage = "Hasło zostało zmienione!"
                $scope.navigate("/#success")
                $window.location.reload();
            },
            function(response){
                alert("porażka");
            });
    }
});
