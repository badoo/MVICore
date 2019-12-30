import com.badoo.mvicore.common.TestSink
import com.badoo.mvicore.common.assertValues
import com.badoo.mvicore.common.element.Reducer
import com.badoo.mvicore.common.feature.ReducerFeature
import kotlin.native.concurrent.freeze
import kotlin.test.Test

class ReducerFeatureTest {

    @Test
    fun reducer_feature_emits_new_state_on_new_wish_when_frozen() {
        val feature = TestReducerFeature().freeze()
        val sink = TestSink<String>()

        feature.connect(sink)

        feature.invoke(0)
        sink.assertValues("", "0")
    }

    class TestReducerFeature(initialState: String = ""): ReducerFeature<Int, String, Nothing>(
        initialState = initialState,
        reducer = reducer { state, wish ->
            state + wish.toString()
        }
    )
}

fun <State, Effect> reducer(block: (state: State, effect: Effect) -> State) =
    object : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State = block(state, effect)
    }
