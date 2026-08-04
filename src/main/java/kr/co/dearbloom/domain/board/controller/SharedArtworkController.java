package kr.co.dearbloom.domain.board.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.dearbloom.domain.board.facade.SharedArtworkFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Shared Artwork", description = "공동보드 공유작품 API")
public class SharedArtworkController {
    private final SharedArtworkFacade sharedArtworkFacade;
}
