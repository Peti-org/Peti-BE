package com.peti.backend;

import com.peti.backend.repository.elastic.ElasticSlotRepository;
import com.peti.backend.utils.ElasticMockDataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class PetiBeApplicationTests {

  @MockitoBean
  private ElasticSlotRepository elasticSlotRepository;

  @MockitoBean
  private ElasticMockDataInitializer elasticMockDataInitializer;


  @Test
  void contextLoads() {
  }

}
