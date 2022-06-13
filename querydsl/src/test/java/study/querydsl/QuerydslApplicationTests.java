package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.QQuerydslTest;
import study.querydsl.entity.QuerydslTest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

	@PersistenceContext
	EntityManager entityManager;

	@Test
	void contextLoads() {
		QuerydslTest test = new QuerydslTest();
		entityManager.persist(test);
		JPAQueryFactory query = new JPAQueryFactory(entityManager);

		QQuerydslTest qQuerydslTest = new QQuerydslTest("h");
		QuerydslTest result = query
				.selectFrom(qQuerydslTest)
				.fetchOne();
		Assertions.assertThat(result).isEqualTo(test);
	}

}
