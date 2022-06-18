package study.querydsl;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @PersistenceContext
    EntityManager em;
    JPAQueryFactory queryFactory;
    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() {
        String qlString =
                "select m from Member m " +
                        "where m.username = :username"; // 파라미터 바인딩 처리해주기
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
    @Test
    public void startQuerydsl() {

        //     public static final QMember member = new QMember("member1");
        /*
        QMember qMember = new QMember("m"); //별칭 직접 지정
        QMember qMember = QMember.member; //기본 인스턴스 사용
         */
        QMember m = QMember.member;
        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))//파라미터 바인딩 처리를 자동으로 해줌
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /*
            member.username.eq("member1") // username = 'member1'
        member.username.ne("member1") //username != 'member1'
        member.username.eq("member1").not() // username != 'member1'
        member.username.isNotNull() //이름이 is not null
        member.age.in(10, 20) // age in (10,20)
        member.age.notIn(10, 20) // age not in (10, 20)
        member.age.between(10,30) //between 10, 30
        member.age.goe(30) // age >= 30
        member.age.gt(30) // age > 30
        member.age.loe(30) // age <= 30
        member.age.lt(30) // age < 30
        member.username.like("member%") //like 검색
        member.username.contains("member") // like ‘%member%’ 검색
        member.username.startsWith("member") //like ‘member%’ 검색
     */


    @Test
    public void resultFetchTest() {
//        List<Member> fetch = queryFactory
//                .selectFrom(member)
//                .fetch();
//
//        /*
//        * com.querydsl.core.NonUniqueResultException:
//        * javax.persistence.NonUniqueResultException: query did not return a unique result: 4
//        * 1개에 대한 조회를 실행했는데 4개가 있어서 나타나는 에러
//        *  */
//        Member fetchOne = queryFactory
//                .selectFrom(member)
//                .fetchOne();
//
//        Member fetchFirst = queryFactory
//                .selectFrom(member)
//                .fetchFirst();

        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        results.getTotal();
        List<Member> content = results.getResults();

        /**
         * queryDsl의 fetchResult의 경우 count를 하기위해선 count용 쿼리를 만들어서 실행해야 하는데,
         * 카운트를 하려는 select 쿼리를 기반으로 count 쿼리를 만들어 실행한다.
         * SELECT COUNT(*) FROM (<original query>).
         *
         * 그런데 이게 단순한 쿼리에서는 잘 동작하는데, 복잡한 쿼리(다중그룹 쿼리)에서는 잘 작동하지 않는다고 한다.
         * groupby having 절을 사용하는 등의 복잡한 쿼리 문에서 예외가 떠버리는 것으로 보이고
         *
         * 더불어 대부분의 dialect에서는 count쿼리가 유효하지만 JPQL에서는 아니란다.
         * 그렇기 때문에 fetchResult보다는 fetch를 사용하고 따로 fetchCount를 사용하거나 list의 count를 사용하는것이 좋을 것 같다!
         */
    }
}
