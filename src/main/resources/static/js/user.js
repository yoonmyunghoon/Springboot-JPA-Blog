let index = {
	init: function(){
        $("#btn-save").on("click", ()=>{ // ''function(){}'' 대신에 화살표함수인 ''()=>{}''를 쓰는 이유: this를 바인딩하기 위해서 
            this.save();
        });
		/*
		$("#btn-login").on("click", ()=>{
            this.login();
        });
		*/
		$("#btn-update").on("click", ()=>{
            this.update();
        });
    },

    save: function(){
        // alert('user의 save함수 호출됨');
		let data = {
			username: $("#username").val(),
			password: $("#password").val(),
			email: $("#email").val()
		};
		
		// console.log(data);
		
		// ajax 호출 시 default가 비동기 호출
		// ajax 통신을 이용해서 3개의 데이터를 json으로 변경하여 insert요청
		$.ajax({
			type: "POST",
			url: "/auth/joinProc",
			data: JSON.stringify(data), // http body데이터 > MIME 타입이 필요
			contentType: "application/json; charset=utf-8", // body 데이터가 어떤 타입인지(MIME)
			dataType: "json" // 응답이 왔을 때(기본적으로 응답은 문자열 형태임), 생긴게 json 모양이라면 javascript 오브젝트로 변경해줌
			// 이 결과가 밑에 함수들의 매개변수에 들어감
		}).done(function(resp){
			if (resp.status === 500) {
				alert("회원가입에 실패하였습니다.");
			} else {
				alert("회원가입이 완료되었습니다.");
				location.href = "/";
			}
		}).fail(function(error){
			// console.log(error);
			alert(JSON.stringify(error));
		});
    },
	/*
	login: function(){
		let data = {
			username: $("#username").val(),
			password: $("#password").val(),
		};
		
		$.ajax({
			type: "POST",
			url: "/api/user/login",
			data: JSON.stringify(data),
			contentType: "application/json; charset=utf-8", 
			dataType: "json"
		}).done(function(resp){
			alert("로그인이 완료되었습니다.");
			location.href = "/";
		}).fail(function(error){
			alert(JSON.stringify(error));
		});
    },
	*/
	update: function(){
		let data = {
			id: $("#id").val(),
			username: $("#username").val(),
			password: $("#password").val(),
			email: $("#email").val()
		};
		
		$.ajax({
			type: "PUT",
			url: "/user",
			data: JSON.stringify(data),
			contentType: "application/json; charset=utf-8", 
			dataType: "json"
		}).done(function(resp){
			alert("회원수정이 완료되었습니다.");
			location.href = "/";
		}).fail(function(error){
			alert(JSON.stringify(error));
		});
    },
}

index.init();
