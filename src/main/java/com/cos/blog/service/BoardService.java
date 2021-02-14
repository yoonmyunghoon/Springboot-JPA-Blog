package com.cos.blog.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cos.blog.dto.ReplySaveRequestDto;
import com.cos.blog.model.Board;
import com.cos.blog.model.User;
import com.cos.blog.repository.BoardRepository;
import com.cos.blog.repository.ReplyRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class BoardService {
	
//	DI 방법
//	1) @Autowired 어노테이션 
//	@Autowired
//	private BoardRepository boardRepository;
//	
//	@Autowired
//	private ReplyRepository replyRepository;

//	2) DI 작동원리 
//	private BoardRepository boardRepository;
//	private ReplyRepository replyRepository;
//	
//	public BoardService(BoardRepository bRepo, ReplyRepository rRepo) {
//		this.boardRepository = bRepo;
//		this.replyRepository = rRepo;
//	}
	
//	3) @RequiredArgsConstructor
	private final BoardRepository boardRepository;
	private final ReplyRepository replyRepository;
	
	@Transactional
	public void 글쓰기(Board board, User user) { // title, content만 받아옴
		board.setCount(0);
		board.setUser(user);
		
		boardRepository.save(board);
	}
	
	@Transactional(readOnly = true)
	public Page<Board> 글목록(Pageable pageable) {
		return boardRepository.findAll(pageable);
	}
	
	@Transactional(readOnly = true)
	public Board 글상세보기(int id) {
		return boardRepository.findById(id)
				.orElseThrow(()->{
					return new IllegalArgumentException("글 상세보기 실패 : 글 아이디를 찾을 수 없습니다. id : "+id);
				});
	}
	
	@Transactional
	public void 글삭제하기(int id, User user) {
		Board board = boardRepository.findById(id).orElseThrow(()->{
			return new IllegalArgumentException("글 삭제 실패 : 글 아이디를 찾을 수 없습니다. id : "+id);
		});
		
		if (board.getUser().getId() != user.getId()) {
			throw new IllegalArgumentException("글 삭제 실패 : 해당 글을 삭제할 권한이 없습니다.");
		}
		boardRepository.deleteById(id);
	}
	
	@Transactional
	public void 글수정하기(int id, Board requestBoard, User user) {
		Board board = boardRepository.findById(id)
				.orElseThrow(()->{
					return new IllegalArgumentException("글 수정 실패 : 글 아이디를 찾을 수 없습니다. id : "+id);
				}); // 영속화 완료 
		
		if (board.getUser().getId() != user.getId()) {
			throw new IllegalArgumentException("글 수정 실패 : 해당 글을 수정할 권한이 없습니다.");
		}
		board.setTitle(requestBoard.getTitle());
		board.setContent(requestBoard.getContent());
		// 해당 함수 종료시(Service가 종료 될 때) 트랜잭션이 종료됨, 이때 더티체킹 -> 자동 업데이트가 됨(db flush)
	}

	@Transactional
	public void 댓글쓰기(ReplySaveRequestDto replySaveRequestDto) {
//		User user = userRepository.findById(replySaveRequestDto.getUserId())
//				.orElseThrow(()->{
//					return new IllegalArgumentException("댓글 작성 실패 : 유저 아이디를 찾을 수 없습니다. id : "+replySaveRequestDto.getUserId());
//				});
//		
//		Board board = boardRepository.findById(replySaveRequestDto.getBoardId())
//				.orElseThrow(()->{
//					return new IllegalArgumentException("댓글 작성 실패 : 글 아이디를 찾을 수 없습니다. id : "+replySaveRequestDto.getBoardId());
//				});
		
//		1)
//		Reply reply = Reply.builder()
//				.user(user)
//				.board(board)
//				.content(replySaveRequestDto.getContent())
//				.build();
		
//		2)
//		Reply reply = new Reply();
//		reply.update(user, board, replySaveRequestDto.getContent());
		
//		3)
		int result =  replyRepository.mSave(replySaveRequestDto.getUserId(), replySaveRequestDto.getBoardId(), replySaveRequestDto.getContent());
		System.out.println(result);
	}
}
