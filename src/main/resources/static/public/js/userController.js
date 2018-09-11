app.controller('userController', function($scope, $http, $cookies, $window) {

    if ($scope.isPage($scope.pages['confirmRegistration'])) {
        console.log("confirmation")
        registerConfirm();
    }

	function invalidEmail() {
        $scope.currentMessage = "Nieprawidło adres email."
        $('#infoModal').modal('show')
	}

	function failedLogin(response)	{
        if (response.status == 404 )
            $scope.currentMessage = "Nieprawidłowy adres email lub hasło."
        else
            $scope.currentMessage = "Błąd logowania."
        $('#infoModal').modal('show')
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
	    console.log("reset")
	    if ($scope.resetEmail == undefined) {
            return;
        }

        var userCredentialsDto = {
            email: $scope.resetEmail
        };

        var response = $http.post("/user/password/reset/request", userCredentialsDto);
        response.then(
            function(response) {
                $('#forgottenPassword').modal('hide')
            },
            function(response){
                $scope.currentMessage = "Wystąpił błąd."
                $('#infoModal').modal('show')
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
                $window.location.href = "/#success"
            },
            function(response){
                $scope.currentMessage = "Wystąpił błąd. Nie udało się zresetować hasła. Spróbuj ponownie lub skontaktuj się z administracją."
                $('#infoModal').modal('show')
            });
    }

    $scope.register = function() {
        if ($scope.email == undefined) {
            invalidEmail();
            return;
        }

        var userDataDto = {
            email: $scope.email,
            password: $scope.password,
            name: $scope.name
        };

        var response = $http.post("/user/register", userDataDto);
        response.then(
            function(response) {
                $scope.currentMessage = "Na podany adres email wysłano wiadomość z potwierdzeniem rejestracji. Możesz aktywować konto i zalogować się."
                $('#infoModal').modal('show')
            },
            function(response){
                $scope.currentMessage = "Wystąpił błąd. Nie udało się dokonać rejestracji."
                $('#infoModal').modal('show')
            });
    }


    $scope.changePassword = function() {

        var passwordDto = {
            password: $scope.password
        };

        var response = $http.post("/user/password/change", passwordDto);
        response.then(
            function(response) {
                $window.location.href = "/#success"
            },
            function(response){
                $scope.currentMessage = "Wystąpił błąd. Nie udało się zresetować hasła. Spróbuj ponownie lub skontaktuj się z administracją."
                $('#infoModal').modal('show')
            });
    }


    function registerConfirm() {
        var id = $scope.params['userId']

        var response = $http.get("/user/register/" + id + "confirm");
        response.then(
            function(response) {
                $scope.confirmationMessage = "Konto aktywowane! Możesz się zalogować."
            },
            function(response){
                $scope.confirmationMessage = "Wystąpił błąd w czasie aktywacji konta."
            });
    }


    $scope.deregister = function() {

        var response = $http.delete("/user/deregister");
        response.then(
            function(response) {
                $scope.currentMessage = "Twoje konto zostało usunięte."
                $('#infoModal').modal('show')
                $cookies.remove('token')
                $window.location.href = "/";
                $window.reload();
            },
            function(response){
                $scope.currentMessage = "Wystąpił błąd. Nie udało się zresetować hasła. Spróbuj ponownie lub skontaktuj się z administracją."
                $('#infoModal').modal('show')
            });
    }


});
