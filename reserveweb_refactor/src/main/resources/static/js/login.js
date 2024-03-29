(function() {
    "use strict";

    const loginController = {
        init : function() {
            loginView.init();
        }
    };

    const loginView = {
        init : function() {
            this.cacheDom();
            this.bindEvent();
        },
        cacheDom : function() {
            this.$email = $("#email");
            this.$password = $("#password");
            this.$loginBtn = $("#loginBtn");
            this.$signupBtn = $("#signupBtn");
        },
        bindEvent : function() {
            this.$loginBtn.on("click", this.clickLoginBtn);
            this.$signupBtn.on("click", function(e) {
                e.preventDefault();

                window.location.href = "/signup";
            })
        },
        clickLoginBtn : function(e) {
            e.preventDefault();

            var payload = {
                email : loginView.$email.val(),
                password : loginView.$password.val()
            };

            $.ajax({
                url: "/api/doLogin",
                type: "POST",
                data: JSON.stringify(payload),
                contentType: "application/json; charset=utf-8",
            }).done(function(response, textStatus, jqXHR) {
                console.log("response : " + response);
                localStorage.setItem("grantType", response.grantType);
                localStorage.setItem("accessToken", response.accessToken);
                localStorage.setItem("refreshToken", response.refreshToken);
                localStorage.setItem("email", response.email);
                localStorage.setItem("userId", response.userId);
                localStorage.setItem("accessTokenValidityTime", response.accessTokenValidityTime);
                localStorage.setItem("refreshTokenValidityTime", response.refreshTokenValidityTime);

                window.location.href = "/mainpage";

            }).fail(function(jqXHR, textStatus, errorThrown) {
                console.log("textStatus : " + textStatus);
                
                alert('아이디 또는 비밀번호가 일치하지 않습니다.');
            });
        }
    };

    loginController.init();
})();
