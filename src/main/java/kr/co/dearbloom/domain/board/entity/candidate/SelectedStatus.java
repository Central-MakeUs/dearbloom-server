package kr.co.dearbloom.domain.board.entity.candidate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SelectedStatus {
    CANDIDATE("후보"),
    SELECTED("선정");

    private final String label;
}
