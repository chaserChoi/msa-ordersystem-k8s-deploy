package beyond.ordersystem.member.repository;

import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.member.dto.MemberResDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
}
