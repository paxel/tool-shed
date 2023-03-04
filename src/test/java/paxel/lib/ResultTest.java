package paxel.lib;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@DisplayName("Write assertions for Results")
class ResultTest {

    @Nested
    @DisplayName("Successful Results")
    class SuccessTest {

        @Test
        @DisplayName("isSuccess is true")
        void okIsOk() {
            assertThat( Result.ok("myName").isSuccess(),is(true));
        }
        @Test
        @DisplayName("should be ResultStatus.SUCCESS")
        void isSuccessEnum() {
            assertThat( Result.ok("myName").getStatus(),is(Result.ResultStatus.SUCCESS));
        }

        @Test
        @DisplayName("can be mapped to other value")
        void canBeMapped() {
            assertThat( Result.ok("myName").mapValue(f->10),is(Result.ok(10)));
        }
    }

    @Nested
    @DisplayName("Failed Results")
    class ErrTest {

        @Test
        @DisplayName("isSuccess is false")
        void failIsNotOk() {
            assertThat( Result.err("myName").isSuccess(),is(false));
        }
        @Test
        @DisplayName("should be ResultStatus.FAIL")
        void isSuccessEnum() {
            assertThat( Result.err("myName").getStatus(),is(Result.ResultStatus.FAIL));
        }

        @Test
        @DisplayName("can be mapped to other error")
        void canBeMapped() {
            assertThat( Result.err("myName").mapError(f->10),is(Result.err(10)));
        }
    }
}