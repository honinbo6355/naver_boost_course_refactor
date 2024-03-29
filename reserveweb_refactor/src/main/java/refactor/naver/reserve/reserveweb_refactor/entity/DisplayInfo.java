package refactor.naver.reserve.reserveweb_refactor.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "display_info")
@Getter
@Setter
@ToString
public class DisplayInfo extends SystemDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "opening_hours")
    private String openingHours;

    @Column(name = "place_name")
    private String placeName;

    @Column(name = "place_lot")
    private String placeLot;

    @Column(name = "place_street")
    private String placeStreet;

    @Column(name = "tel")
    private String telephone;

    private String homepage;
    private String email;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToOne(mappedBy = "displayInfo", fetch = FetchType.LAZY)
    private DisplayInfoImage displayInfoImage;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "displayInfo")
    private Set<ReservationInfo> reservationInfos;
}
