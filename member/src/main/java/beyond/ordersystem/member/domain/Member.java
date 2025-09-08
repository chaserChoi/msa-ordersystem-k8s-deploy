package beyond.ordersystem.member.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
// jpql을 제외하고 모든 조회 쿼리에 where del_yn = "N" 붙이는 효과
//@Where(clause = "del_yn = 'N'")
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Builder.Default
    @Setter
    private String delYn = "N";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    /*@OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Product> products = new ArrayList<>();*/

//    public void deleteMember(String delYn) {
//        this.delYn = delYn;
//    }
}
