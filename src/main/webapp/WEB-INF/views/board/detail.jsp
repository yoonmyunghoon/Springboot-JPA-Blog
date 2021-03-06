<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../layout/header.jsp"%>

<div class="container">

	<div>
		No : <span id="id"><i>${board.id}</i></span> &nbsp; &nbsp;/&nbsp; &nbsp; Writer : <span><i>${board.user.username}</i></span>
	</div>
	<hr />

	<div class="card">
		<div class="card-header">
			<h3>Title : ${board.title}</h3>
		</div>
		<div class="card-body">
			<div>Content : ${board.content}</div>
		</div>
	</div>
	<br>

	<div class="float-right">
		<button class="btn btn-secondary" onclick="history.back()">돌아가기</button>
		<c:if test="${board.user.id == principal.user.id}">
			<a href="/board/${board.id}/updateForm" class="btn btn-warning">수정</a>
			<button id="btn-delete" class="btn btn-danger">삭제</button>
		</c:if>
	</div>
	<br> <br>
	<hr>

	<div class="card">
		<form>
			<input type="hidden" id="userId" value="${principal.user.id}" />
			<input type="hidden" id="boardId" value="${board.id}" />
			<div class="card-body">
				<span>Comment :</span>
				<textarea id="reply-content" class="form-control" rows="1"></textarea>
			</div>
			<div class="card-footer">
				<button type="button" id="btn-reply-save" class="btn btn-primary">등록</button>
			</div>
		</form>
	</div>
	<br>
	<div class="card">
		<div class="card-header">댓글 리스트</div>
		<ul id="reply-box" class="list-group">
			<c:forEach var="reply" items="${board.replys}">
				
				<li id="reply-${reply.id}" class="list-group-item d-flex justify-content-between">
					<div>${reply.content}</div>
					<div class="d-flex">
						<div class="font-italic">작성자 : ${reply.user.username} &nbsp;</div>
						<!-- <button class="badge btn-warning">수정</button> -->
						<c:if test="${reply.user.username == principal.user.username}">
							<button onClick="index.replyDelete(${board.id}, ${reply.id})" class="badge btn-danger">삭제</button>
						</c:if>
					</div>
				</li>
				
			</c:forEach>
		</ul>
	</div>

</div>

<script src="/js/board.js"></script>
<%@ include file="../layout/footer.jsp"%>