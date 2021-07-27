import kotlin.properties.Delegates
import kotlin.reflect.KProperty

//1
class ListA {
    val list = mutableListOf<String>()
    operator fun get(i: Int) = list[i]
    operator fun plus(b: String) = run {
        list += b
        this
    }

    infix fun add(b: String) = plus(b)
}
//val list = ListA() + "abc"
//println( list[0] )

class ListA1 {
    val list = mutableListOf<String>()
    operator fun get(i: Int) = list[i]
    operator fun plus(b: String) = run {
        list += b
        this
    }

    infix fun add(b: String) = plus(b)

    // 2
    infix fun <T> T.combine(v: T) = mutableListOf(this, v)
    infix fun <T> MutableList<T>.combine(v: T) = run {
        this.add(v)
        this
    }
}
//val list = ListA() + "abc"
//println( list[0] )
//println( (list add "def")[1] )


//val list = 10 combine 20 combine 30 combine 40
//println("${JSON.stringify(list)}")

//ReadWriteProperty
public interface ReadWriteProperty<in R, T> {
    public operator fun getValue(thisRef: R, property: KProperty<*>): T
    public operator fun setValue(thisRef: R, property: KProperty<*>, value: T)
}

//NotNull
private class NotNullVar<T : Any>() : ReadWriteProperty<Any?, T> {
    private var value: T? = null
    public override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property should be initialized before get.")
    }

    public override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

//lateinit var
class Notnull {
    lateinit var a: String
    fun action(v: String) {
        a = v
        println(a)
    }
}

private class NotNullVar<T : Any>() : ReadWriteProperty<Any?, T> {
    private var value: T? = null
    public override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property should be initialized before get.")
    }

    public override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

class Notnull {
    var a: String by Delegates.notNull()
    fun action(v: String) {
        a = v
        println(a)
    }
}

//decorator
class Dele(val deco: String) : ReadWriteProperty<Any?, String> {
    private var value: String? = null
    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return "$deco$value"
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        this.value = value
    }
}

class CustomDele(deco: String) {
    var a by Dele(deco)
    fun action(v: String) {
        a = v
        println(a)
    }
}

val cd = CustomDele("^^;; ")
//cd.action("abc")

//lazy
public interface Lazy<out T> {
    public val value: T
    public fun isInitialized(): Boolean
}

class Immun(override val value: String) : Lazy<String> {
    override fun isInitialized() = true
}

class CustomImmun {
    val a by Immun("abc")
    fun action(v: String) {
        println(a)
    }
}

class Keys<T>(map: Map<T, Any>) : Lazy<Set<T>> {
    override val value = map.keys
    override fun isInitialized() = true
}

class CustomKeys(val map: Map<String, Any>) {
    val keys by Keys(map)
    fun action() {
        println("${JSON.stringify(keys)}")
    }
}

//map delegation
class MapDele(var dele: MutableMap<String, Any?>) {
    val a: String by dele
    val b: Int by dele
}

val md = MapDele(mutableMapOf("a" to "abc", "b" to 3))
//println("md - ${md.a}")
//println("md - ${md.b}")
//md.dele = mutableMapOf("a" to "def", "b" to 5)
//println("md - ${md.a}")
//println("md - ${md.b}")

//by strategy
interface Mobile {
    fun move(): String
    fun stop(): String
}

class Car(val name: String) : Mobile {
    override fun move() = "$name 이동"
    override fun stop() = "$name 정지"
}

class FastCar(car: Mobile) : Mobile by car {
    fun fastSpeed() = "빠른 이동"
}

val fcar = FastCar(Car("BMW"))
//println("fcar - ${fcar.move()}")
//println("fcar - ${fcar.stop()}")
//println("fcar - ${fcar.fastSpeed()}")

//by object
//interface Mobile{
//    fun move():String
//    fun stop():String
//}
//class Car(val name:String):Mobile{
//    override fun move()= "$name 이동"
//    override fun stop() = "$name 정지"
//}
class UltraCar(var car: Mobile) : Mobile by
car {
    fun UltraSpeed() = "초빠른 이동"
}

val ucar = UltraCar(Car("택시"))

//println("ucar - ${ucar.move()}")
//println("ucar - ${ucar.stop()}")
//println("ucar - ${ucar.UltraSpeed()}")
//ucar.car = Car("야간버스")
//println("ucar - ${ucar.move()}")
class DogCar : Mobile by object : Mobile {
    val name = "아인"
    override fun move() = "$name 달려"
    override fun stop() = "$name 멈춰"
} {
    fun fastSpeed() = "개빠름"
}

val dcar = DogCar()
//println("dcar - ${dcar.move()}")
//println("dcar - ${dcar.stop()}")
//println("dcar - ${dcar.fastSpeed()}")

//by by by
interface AA {
    fun a()
}

interface BB {
    fun b()
}

class AB0 : AA by object : AA {
    override fun a() {
//a
    }
}, BB by object : BB {
    override fun b() {
//b
    }
} {
//AB0
}

class AB1(v: AA = object : AA {
    override fun a() {
//a
    }
}) : AA by v {
//AB1
}

class AB2 : AA by object : AA by object : AA {
    override fun a() {
//a
    }
} {
//obj
} {
//AB2
}