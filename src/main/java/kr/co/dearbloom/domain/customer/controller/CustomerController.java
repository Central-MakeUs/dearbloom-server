package kr.co.dearbloom.domain.customer.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.dearbloom.domain.customer.facade.CustomerFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "고객 API")
public class CustomerController {
    private final CustomerFacade customerFacade;

//    @PostMapping

}
