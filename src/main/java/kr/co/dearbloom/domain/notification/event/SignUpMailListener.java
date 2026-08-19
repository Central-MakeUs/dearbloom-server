package kr.co.dearbloom.domain.notification.event;

import kr.co.dearbloom.domain.auth.entity.OAuthAccount;
import kr.co.dearbloom.domain.auth.repository.OAuthAccountRepository;
import kr.co.dearbloom.domain.auth.util.ApplePrivateRelayEmail;
import kr.co.dearbloom.domain.member.event.MemberSignedUpEvent;
import kr.co.dearbloom.domain.notification.message.SignUpMailFactory;
import kr.co.dearbloom.domain.notification.service.MailSendService;
import kr.co.dearbloom.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 온보딩 완료 → 가입 안내 메일.
 *
 * <p><b>{@code AFTER_COMMIT} 이어야 한다.</b> 커밋 전에 보내면 롤백된 가입의 메일이 나가는데, 메일은 취소가 안 된다.
 *
 * <p><b>{@code @Async} 여야 한다.</b> SMTP 왕복이 수백 ms~수 초라, 동기로 하면 그만큼 온보딩 응답이 늦어진다.
 * 메일 주소 조회도 여기서 하므로 온보딩 트랜잭션에는 쿼리가 늘지 않는다.
 *
 * <p><b>트랜잭션은 {@code REQUIRES_NEW} 여야 한다.</b> AFTER_COMMIT 은 원래 트랜잭션이 이미 커밋된 뒤라
 * 거기에 참여할 수 없고, Spring 이 그 조합을 기동 시점에 막는다. 새 트랜잭션을 열어 주소를 조회한다.
 *
 * <p>예외를 삼킨다 — 메일 실패가 이미 커밋된 가입에 영향을 주면 안 된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SignUpMailListener {
    private final OAuthAccountRepository oAuthAccountRepository;
    private final SignUpMailFactory signUpMailFactory;
    private final MailSendService mailSendService;

    @Async(AsyncConfig.MAIL_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onMemberSignedUp(MemberSignedUpEvent event) {
        try {
            OAuthAccount account = oAuthAccountRepository.findByMember_MemberId(event.memberId()).orElse(null);
            if (account == null) {
                log.warn("[Mail] 소셜 계정을 찾지 못해 가입 메일을 건너뜀 — memberId={}", event.memberId());
                return;
            }
            if (!isDeliverable(account)) {
                // Apple 이 이메일을 주지 않아 만들어 넣은 주소다. 보내면 반송된다.
                log.info("[Mail] 발송 가능한 주소가 없어 가입 메일을 건너뜀 — memberId={}", event.memberId());
                return;
            }
            mailSendService.send(
                    account.getEmail(),
                    signUpMailFactory.signUp(event.profileName(), event.role(), account.getOauthProvider()));
        } catch (Exception e) {
            log.warn("[Mail] 가입 안내 발송 실패 — memberId={}, {}", event.memberId(), e.getMessage());
        }
    }

    private boolean isDeliverable(OAuthAccount account) {
        String email = account.getEmail();
        if (email == null || email.isBlank()) {
            return false;
        }
        return !ApplePrivateRelayEmail.isPlaceholder(email, account.getOauthId());
    }
}
