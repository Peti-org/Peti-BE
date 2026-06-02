package com.peti.backend.service.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
class EmailChangeNotifierTest {

  @Mock
  private JavaMailSender mailSender;

  @InjectMocks
  private EmailChangeNotifier notifier;

  @Test
  @DisplayName("notifyEmailChanged - sends two emails: to old and new address")
  void notifyEmailChanged_sendsBothEmails() {
    ReflectionTestUtils.setField(notifier, "fromAddress", "noreply@peti.com");

    notifier.notifyEmailChanged("old@example.com", "new@example.com");

    ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender, times(2)).send(captor.capture());

    SimpleMailMessage toOld = captor.getAllValues().get(0);
    assertThat(toOld.getTo()).containsExactly("old@example.com");
    assertThat(toOld.getSubject()).contains("has been changed");
    assertThat(toOld.getText()).contains("new@example.com");

    SimpleMailMessage toNew = captor.getAllValues().get(1);
    assertThat(toNew.getTo()).containsExactly("new@example.com");
    assertThat(toNew.getSubject()).contains("confirmed");
  }

  @Test
  @DisplayName("notifyEmailChanged - handles mail send failure gracefully")
  void notifyEmailChanged_handlesFailure() {
    ReflectionTestUtils.setField(notifier, "fromAddress", "noreply@peti.com");
    doThrow(new MailSendException("SMTP error"))
        .when(mailSender).send(any(SimpleMailMessage.class));

    assertThatCode(() -> notifier.notifyEmailChanged("old@example.com", "new@example.com"))
        .doesNotThrowAnyException();
  }
}

