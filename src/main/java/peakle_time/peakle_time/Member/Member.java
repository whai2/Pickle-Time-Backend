package peakle_time.peakle_time.Member;

import jakarta.persistence.*;
import lombok.*;
import peakle_time.peakle_time.global.BaseEntity;
import peakle_time.peakle_time.Image.Image;
import peakle_time.peakle_time.global.Location;
import peakle_time.peakle_time.Participant.Participant;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String loginId;

    @Column(nullable = false)
    private String nickname;

    private String email;
    private String password;
    private Long age;

    @OneToOne
    private Image image;

    @Embedded
    private Location location;
    private String socialType;

    @OneToMany
    @JoinColumn(name = "memberId")
    private List<Participant> participants;

    // 생성자 혹은 빌더

}
