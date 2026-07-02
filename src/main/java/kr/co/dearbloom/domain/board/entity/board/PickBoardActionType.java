package kr.co.dearbloom.domain.board.entity.board;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PickBoardActionType {
    ADDED("작품 추가"),
    REMOVED("작품 삭제"),
    SELECTED("선정"),
    UNSELECTED("선정 취소");

    private final String label;
}
