package com.example.parking.domain.parkingLot.service;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import com.example.parking.domain.parkingLot.external.client.ParkingOpenApiClient;
import com.example.parking.domain.parkingLot.external.dto.ParkingApiDto;
import com.example.parking.domain.parkingLot.repository.ParkingLotRepository;
import com.example.parking.domain.parkingspot.service.ParkingSpotService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingLotSyncServiceTest {

    @Mock
    private ParkingOpenApiClient parkingOpenApiClient;

    @Mock
    private ParkingLotRepository parkingLotRepository;

    @Mock
    private ParkingSpotService parkingSpotService;

    @InjectMocks
    private ParkingLotSyncService parkingLotSyncService;

    @Nested
    @DisplayName("syncParkingLots")
    class SyncParkingLotsTest {

        @Test
        @DisplayName("externalId가 null이면 건너뛴다")
        void skip_whenExternalIdIsNull() {
            // given
            ParkingApiDto.ParkingLotItem item = createItem(
                    null, "역삼 공영주차장", "서울 강남구 역삼동", 10.0
            );

            given(parkingOpenApiClient.fetchParkingLots())
                    .willReturn(createResponse(List.of(item)));

            // when
            parkingLotSyncService.syncParkingLots();

            // then
            verify(parkingLotRepository, never()).findByExternalId(anyString());
            verify(parkingLotRepository, never()).save(any(ParkingLot.class));
            verify(parkingSpotService, never()).createSpots(any(ParkingLot.class), anyInt());
        }

        @Test
        @DisplayName("externalId가 blank이면 건너뛴다")
        void skip_whenExternalIdIsBlank() {
            // given
            ParkingApiDto.ParkingLotItem item = createItem(
                    "   ", "삼성 공영주차장", "서울 강남구 삼성동", 20.0
            );

            given(parkingOpenApiClient.fetchParkingLots())
                    .willReturn(createResponse(List.of(item)));

            // when
            parkingLotSyncService.syncParkingLots();

            // then
            verify(parkingLotRepository, never()).findByExternalId(anyString());
            verify(parkingLotRepository, never()).save(any(ParkingLot.class));
            verify(parkingSpotService, never()).createSpots(any(ParkingLot.class), anyInt());
        }

        @Test
        @DisplayName("기존 주차장이 있으면 updateInfo만 수행한다")
        void updateExistingParkingLot() {
            // given
            String externalId = "P-001";
            ParkingApiDto.ParkingLotItem item = createItem(
                    externalId, "수정된 주차장명", "서울 강남구 대치동", 50.0
            );

            ParkingLot existingParkingLot = spy(
                    ParkingLot.of("P-001", "기존 주차장명", "서울 강남구 역삼동", 10)
            );

            given(parkingOpenApiClient.fetchParkingLots())
                    .willReturn(createResponse(List.of(item)));
            given(parkingLotRepository.findByExternalId(externalId))
                    .willReturn(Optional.of(existingParkingLot));

            // when
            parkingLotSyncService.syncParkingLots();

            // then
            verify(parkingLotRepository).findByExternalId(externalId);
            verify(existingParkingLot).updateInfo("수정된 주차장명", "서울 강남구 대치동", 50);

            verify(parkingLotRepository, never()).save(any(ParkingLot.class));
            verify(parkingSpotService, never()).createSpots(any(ParkingLot.class), anyInt());
        }

        @Test
        @DisplayName("기존 주차장이 없으면 저장한다")
        void saveNewParkingLot() {
            // given
            String externalId = "P-002";
            ParkingApiDto.ParkingLotItem item = createItem(
                    externalId, "신규 주차장", "서울 강남구 청담동", 30.0
            );

            ParkingLot savedParkingLot = ParkingLot.of(
                    externalId, "신규 주차장", "서울 강남구 청담동", 30
            );
            setField(savedParkingLot, "id", 1L);

            given(parkingOpenApiClient.fetchParkingLots())
                    .willReturn(createResponse(List.of(item)));
            given(parkingLotRepository.findByExternalId(externalId))
                    .willReturn(Optional.empty());
            given(parkingLotRepository.save(any(ParkingLot.class)))
                    .willReturn(savedParkingLot);

            // when
            parkingLotSyncService.syncParkingLots();

            // then
            ArgumentCaptor<ParkingLot> captor = ArgumentCaptor.forClass(ParkingLot.class);
            verify(parkingLotRepository).save(captor.capture());

            ParkingLot savedArg = captor.getValue();
            assertThat(savedArg.getExternalId()).isEqualTo("P-002");
            assertThat(savedArg.getName()).isEqualTo("신규 주차장");
            assertThat(savedArg.getAddress()).isEqualTo("서울 강남구 청담동");
            assertThat(savedArg.getTotalSpot()).isEqualTo(30);
        }

        @Test
        @DisplayName("신규 주차장이고 totalSpot이 0보다 크면 자리 생성을 수행한다")
        void createSpots_whenNewParkingLotAndTotalSpotPositive() {
            // given
            String externalId = "P-003";
            ParkingApiDto.ParkingLotItem item = createItem(
                    externalId, "신규 주차장", "서울 강남구 논현동", 15.0
            );

            ParkingLot savedParkingLot = ParkingLot.of(
                    externalId, "신규 주차장", "서울 강남구 논현동", 15
            );
            setField(savedParkingLot, "id", 2L);

            given(parkingOpenApiClient.fetchParkingLots())
                    .willReturn(createResponse(List.of(item)));
            given(parkingLotRepository.findByExternalId(externalId))
                    .willReturn(Optional.empty());
            given(parkingLotRepository.save(any(ParkingLot.class)))
                    .willReturn(savedParkingLot);

            // when
            parkingLotSyncService.syncParkingLots();

            // then
            verify(parkingSpotService).createSpots(savedParkingLot, 15);
        }

        @Test
        @DisplayName("신규 주차장이어도 totalSpot이 null이면 자리 생성을 하지 않는다")
        void doNotCreateSpots_whenTotalSpotIsNull() {
            // given
            String externalId = "P-004";
            ParkingApiDto.ParkingLotItem item = createItem(
                    externalId, "신규 주차장", "서울 강남구 개포동", null
            );

            ParkingLot savedParkingLot = ParkingLot.of(
                    externalId, "신규 주차장", "서울 강남구 개포동", null
            );
            setField(savedParkingLot, "id", 3L);

            given(parkingOpenApiClient.fetchParkingLots())
                    .willReturn(createResponse(List.of(item)));
            given(parkingLotRepository.findByExternalId(externalId))
                    .willReturn(Optional.empty());
            given(parkingLotRepository.save(any(ParkingLot.class)))
                    .willReturn(savedParkingLot);

            // when
            parkingLotSyncService.syncParkingLots();

            // then
            verify(parkingSpotService, never()).createSpots(any(ParkingLot.class), anyInt());
        }

        @Test
        @DisplayName("신규 주차장이어도 totalSpot이 0이면 자리 생성을 하지 않는다")
        void doNotCreateSpots_whenTotalSpotIsZero() {
            // given
            String externalId = "P-005";
            ParkingApiDto.ParkingLotItem item = createItem(
                    externalId, "신규 주차장", "서울 강남구 수서동", 0.0
            );

            ParkingLot savedParkingLot = ParkingLot.of(
                    externalId, "신규 주차장", "서울 강남구 수서동", 0
            );
            setField(savedParkingLot, "id", 4L);

            given(parkingOpenApiClient.fetchParkingLots())
                    .willReturn(createResponse(List.of(item)));
            given(parkingLotRepository.findByExternalId(externalId))
                    .willReturn(Optional.empty());
            given(parkingLotRepository.save(any(ParkingLot.class)))
                    .willReturn(savedParkingLot);

            // when
            parkingLotSyncService.syncParkingLots();

            // then
            verify(parkingSpotService, never()).createSpots(any(ParkingLot.class), anyInt());
        }
    }

    private ParkingApiDto.Response createResponse(List<ParkingApiDto.ParkingLotItem> items) {
        ParkingApiDto.ApiResult apiResult = new ParkingApiDto.ApiResult(
                "INFO-000",
                "정상 처리되었습니다."
        );

        ParkingApiDto.ParkInfo parkInfo = new ParkingApiDto.ParkInfo(
                items.size(),
                apiResult,
                items
        );

        return new ParkingApiDto.Response(parkInfo);
    }

    private ParkingApiDto.ParkingLotItem createItem(
            String pkltCd,
            String pkltNm,
            String addr,
            Double tpkct
    ) {
        return new ParkingApiDto.ParkingLotItem(
                pkltCd,
                pkltNm,
                addr,
                tpkct
        );
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
