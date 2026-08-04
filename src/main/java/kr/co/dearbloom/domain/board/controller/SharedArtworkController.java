package kr.co.dearbloom.domain.board.controller;

import kr.co.dearbloom.domain.board.facade.SharedArtworkFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SharedArtworkController {
    private final SharedArtworkFacade sharedArtworkFacade;
}
