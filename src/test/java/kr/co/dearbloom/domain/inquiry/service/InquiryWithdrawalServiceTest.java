package kr.co.dearbloom.domain.inquiry.service;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.repository.ArtistRepository;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.repository.CustomerRepository;
import kr.co.dearbloom.domain.inquiry.entity.Inquiry;
import kr.co.dearbloom.domain.inquiry.entity.InquiryStatus;
import kr.co.dearbloom.domain.inquiry.repository.InquiryRepository;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/** 탈퇴·해지 시 진행 중 문의 자동 취소 로직 검증(상태 전이 + 사유·role 기록). */
@ExtendWith(MockitoExtension.class)
class InquiryWithdrawalServiceTest {
    @Mock InquiryRepository inquiryRepository;
    @Mock InquiryHistoryCommandService historyCommandService;
    @Mock CustomerRepository customerRepository;
    @Mock ArtistRepository artistRepository;

    @InjectMocks InquiryWithdrawalService service;

    private final Member member = Mockito.mock(Member.class);

    private Inquiry inquiryWith(InquiryStatus status) {
        return Inquiry.builder().status(status).build();
    }

    @Test
    void 고객해지_진행중은_문의취소_예약완료는_예약취소로_바꾸고_사유를_기록한다() {
        Customer customer = Mockito.mock(Customer.class);
        given(customer.getCustomerId()).willReturn(10L);
        given(customerRepository.findByMember(member)).willReturn(Optional.of(customer));

        Inquiry inProgress = inquiryWith(InquiryStatus.IN_PROGRESS);
        Inquiry reserved = inquiryWith(InquiryStatus.RESERVED);
        given(inquiryRepository.findByCustomerIdAndStatusIn(eq(10L), any()))
                .willReturn(List.of(inProgress, reserved));

        service.cancelForCustomerRevoke(member);

        assertThat(inProgress.getStatus()).isEqualTo(InquiryStatus.INQUIRY_CANCELED);
        assertThat(reserved.getStatus()).isEqualTo(InquiryStatus.RESERVATION_CANCELED);
        verify(historyCommandService).record(inProgress, InquiryStatus.IN_PROGRESS,
                MemberRole.CUSTOMER, "고객 역할 해지로 자동 취소");
        verify(historyCommandService).record(reserved, InquiryStatus.RESERVED,
                MemberRole.CUSTOMER, "고객 역할 해지로 자동 취소");
        // 고객 해지는 작가측 문의를 건드리지 않는다
        verifyNoInteractions(artistRepository);
    }

    @Test
    void 작가해지_작가측_예약완료를_예약취소로_바꾸고_ARTIST_사유를_기록한다() {
        Artist artist = Mockito.mock(Artist.class);
        given(artist.getArtistId()).willReturn(20L);
        given(artistRepository.findByMember(member)).willReturn(Optional.of(artist));

        Inquiry reserved = inquiryWith(InquiryStatus.RESERVED);
        given(inquiryRepository.findByArtistIdAndStatusIn(eq(20L), any()))
                .willReturn(List.of(reserved));

        service.cancelForArtistRevoke(member);

        assertThat(reserved.getStatus()).isEqualTo(InquiryStatus.RESERVATION_CANCELED);
        verify(historyCommandService).record(reserved, InquiryStatus.RESERVED,
                MemberRole.ARTIST, "작가 역할 해지로 자동 취소");
        verifyNoInteractions(customerRepository);
    }

    @Test
    void 탈퇴는_고객측과_작가측_문의를_모두_취소한다() {
        Customer customer = Mockito.mock(Customer.class);
        Artist artist = Mockito.mock(Artist.class);
        given(customer.getCustomerId()).willReturn(10L);
        given(artist.getArtistId()).willReturn(20L);
        given(customerRepository.findByMember(member)).willReturn(Optional.of(customer));
        given(artistRepository.findByMember(member)).willReturn(Optional.of(artist));

        Inquiry asCustomer = inquiryWith(InquiryStatus.IN_PROGRESS);
        Inquiry asArtist = inquiryWith(InquiryStatus.RESERVED);
        given(inquiryRepository.findByCustomerIdAndStatusIn(eq(10L), any())).willReturn(List.of(asCustomer));
        given(inquiryRepository.findByArtistIdAndStatusIn(eq(20L), any())).willReturn(List.of(asArtist));

        service.cancelAllForWithdrawal(member);

        assertThat(asCustomer.getStatus()).isEqualTo(InquiryStatus.INQUIRY_CANCELED);
        assertThat(asArtist.getStatus()).isEqualTo(InquiryStatus.RESERVATION_CANCELED);
        verify(historyCommandService).record(asCustomer, InquiryStatus.IN_PROGRESS,
                MemberRole.CUSTOMER, "회원 탈퇴로 자동 취소");
        verify(historyCommandService).record(asArtist, InquiryStatus.RESERVED,
                MemberRole.ARTIST, "회원 탈퇴로 자동 취소");
    }

    @Test
    void 취소할_문의가_없으면_아무것도_기록하지_않는다() {
        given(customerRepository.findByMember(member)).willReturn(Optional.empty());

        service.cancelForCustomerRevoke(member);

        verifyNoInteractions(historyCommandService);
    }
}
