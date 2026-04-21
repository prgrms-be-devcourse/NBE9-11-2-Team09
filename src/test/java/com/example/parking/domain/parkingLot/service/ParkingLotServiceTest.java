package com.example.parking.domain.parkingLot.service;

import com.example.parking.domain.parkingLot.dto.ParkingLotResDto;
import com.example.parking.domain.parkingLot.entity.ParkingLot;
import com.example.parking.domain.parkingLot.repository.ParkingLotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 테스트 의존성 이슈로 현재는 목 기반 단위 테스트만 우선 작성.
 * 실제 동작은 실행 환경에서 별도 검증함.
 */
@ExtendWith(MockitoExtension.class)
class ParkingLotServiceTest {

    @Mock
    private ParkingLotRepository parkingLotRepository;

    @InjectMocks
    private ParkingLotService parkingLotService;

    private ParkingLot parkingLot1;
    private ParkingLot parkingLot2;

    @BeforeEach
    void setUp() {
        parkingLot1 = createParkingLot(
                1L,
                "EXT-001",
                "역삼 공영주차장",
                "서울 강남구 역삼동 123",
                100
        );

        parkingLot2 = createParkingLot(
                2L,
                "EXT-002",
                "삼성 공영주차장",
                "서울 강남구 삼성동 456",
                80
        );
    }

    @Nested
    @DisplayName("전체 주차장 조회, 동 검색")
    class FindAllTest {

        @Test
        @DisplayName("dong이 null이면 전체 주차장 목록을 조회한다")
        void findAll_withNullDong_returnsAllParkingLots() {
            // given
            given(parkingLotRepository.findAll()).willReturn(List.of(parkingLot1, parkingLot2));

            // when
            List<ParkingLotResDto> result = parkingLotService.findAll(null);

            // then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(ParkingLotResDto::name)
                    .containsExactly("역삼 공영주차장", "삼성 공영주차장");

            verify(parkingLotRepository).findAll();
        }

        @Test
        @DisplayName("dong이 공백이면 전체 주차장 목록을 조회한다")
        void findAll_withBlankDong_returnsAllParkingLots() {
            // given
            given(parkingLotRepository.findAll()).willReturn(List.of(parkingLot1, parkingLot2));

            // when
            List<ParkingLotResDto> result = parkingLotService.findAll("   ");

            // then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(ParkingLotResDto::name)
                    .containsExactly("역삼 공영주차장", "삼성 공영주차장");

            verify(parkingLotRepository).findAll();
        }

        @Test
        @DisplayName("dong이 있으면 해당 동이 포함된 주소의 주차장 목록을 조회한다")
        void findAll_withDong_returnsFilteredParkingLots() {
            // given
            given(parkingLotRepository.findByAddressContaining("역삼동"))
                    .willReturn(List.of(parkingLot1));

            // when
            List<ParkingLotResDto> result = parkingLotService.findAll("역삼동");

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("역삼 공영주차장");

            verify(parkingLotRepository).findByAddressContaining("역삼동");
        }

        @Test
        @DisplayName("검색 결과가 없으면 빈 리스트를 반환한다")
        void findAll_withDong_returnsEmptyList() {
            // given
            given(parkingLotRepository.findByAddressContaining("청담동"))
                    .willReturn(List.of());

            // when
            List<ParkingLotResDto> result = parkingLotService.findAll("청담동");

            // then
            assertThat(result).isEmpty();

            verify(parkingLotRepository).findByAddressContaining("청담동");
        }
    }

    @Nested
    @DisplayName("특정 주차장 조회")
    class FindByIdTest {

        @Test
        @DisplayName("id로 주차장을 조회할 수 있다")
        void findById_success() {
            // given
            given(parkingLotRepository.findById(1L)).willReturn(Optional.of(parkingLot1));

            // when
            ParkingLotResDto result = parkingLotService.findById(1L);

            // then
            assertThat(result.name()).isEqualTo("역삼 공영주차장");
            verify(parkingLotRepository).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 id로 조회하면 예외가 발생한다")
        void findById_fail() {
            // given
            given(parkingLotRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> parkingLotService.findById(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 주차장이 없습니다.");

            verify(parkingLotRepository).findById(999L);
        }
    }

    private ParkingLot createParkingLot(Long id, String externalId, String name, String address, Integer totalSpot) {
        ParkingLot parkingLot = ParkingLot.builder()
                .externalId(externalId)
                .name(name)
                .address(address)
                .totalSpot(totalSpot)
                .price(1000)
                .operationStartTime(LocalTime.of(0, 0))
                .operationEndTime(LocalTime.of(23, 59))
                .build();

        setField(parkingLot, "id", id);
        return parkingLot;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
