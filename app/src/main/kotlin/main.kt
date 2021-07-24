import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.css.CSSStyleDeclaration
import org.w3c.dom.events.KeyboardEvent
import org.w3c.fetch.RequestInit
import kotlin.browser.document

fun main() {
    app()
}

fun app(){
    document.querySelector("#base")?.innerHTML = """<input id="input"/><div id="result"></div>"""
    document.querySelector("#input")?.addEventListener("keyup", {
        if((it as KeyboardEvent).keyCode != 13) return@addEventListener
        val input = it.target as HTMLInputElement
        val v = input.value
        document.querySelector("#result")?.innerHTML = "$v = ${calc(v)}"
        input.value = ""
    })
}
val cleanUp = """[^.\d-+*\/]""".toRegex()
val mulDiv = """((?:\+-)?[.\d]+)([*\/])((?:\+-)?[.\d]+)""".toRegex()
val paren = """\(([^()]*)\)""".toRegex()
fun ex(v:String) = v.replace(cleanUp, "").replace("-", "+-").replace(mulDiv){
    val (_, left, op, right) = it.groupValues
    val l = left.replace("+", "").toDouble()
    val r = right.replace("+", "").toDouble()
    "${if(op == "*") l * r else l / r}".replace("-", "+-")
}.split('+').fold(0.0) { acc, v -> acc + if(v.isBlank()) 0.0 else v.toDouble()}
fun calc(v:String):Double{
    var r = v
    while(paren.containsMatchIn(r)) r = r.replace(paren){"${ex(it.groupValues[1])}"}
    return ex(r)
}

abstract class Node(val parent: Element?)
class Element(val tagName:String, parent:Element?):Node(parent){
    val attributes = mutableMapOf<String, String>()
    val children = mutableListOf<Node>()
}
class TextNode(val text:String, parent:Element?):Node(parent)

fun parseHTML(v:String) = parse(Element("root", null), v)
val rex = """<([a-zA-Z]+)((?:\s+[a-zA-Z-]+(?:\s*=\s*"[^"]*")?)*)\s*/?""".toRegex()
tailrec fun parse(parent:Element, v:String):Element = if(v[0] != '<'){
    if(v.isEmpty()) parent
    else{
        val next = v.indexOf('<')
        parent.children += TextNode(v.substring(0, if(next == -1) v.length else next), parent)
        if(next == -1) parent else parse(parent, v.substring(next))
    }
}else{
    val next = v.indexOf('>')
    if(v[1] == '/'){
        if(parent.parent == null) parent
        else parse(parent.parent, v.substring(next + 1))
    }else{
        val isClose = v[next - 1] == '/'
        val matches = rex.matchEntire(v.substring(0, next))?.groupValues!!
        val el = Element(matches[1], parent)
        if(matches[2].isNotBlank()) matches[2].trim().split(' ').forEach {
            val kv = it.split('=').map { it.trim() }
            el.attributes[kv[0]] = kv[1].replace("\"", "")
        }
        parent.children += el
        parse(if(isClose) parent else el, v.substring(next + 1))
    }
}

fun printElement(el:Element, indent:Int = 0){
    el.children.forEach {
        if(it is Element){
            println("${"-".repeat(indent)}Element ${it.tagName}")
            if(it.attributes.isNotEmpty()){
                println("${" ".repeat(indent + 2)}Attribute ${
                    it.attributes.map{(k, v)->"$k = '$v'"}.joinToString(" ")
                }")
            }
            printElement(it, indent + 1)
        }else if(it is TextNode){
            println("${"-".repeat(indent)}Text '${it.text}'")
        }
    }
}
val request = RequestBuilder("http://apiServer"){
    method = Method.POST
    form["name"] = "hika"
    form["email"] = "hika@bsidesoft.com"
    timeout = 5000
    ok = {}
    fail = {}
}

enum class Method{POST, GET}
class Request(
        val url:String,
        val method: RequestInit,
        val form:MutableMap<String, String>?,
        val timeout:Int,
        val ok:listener?,
        val fail:listener?
)
typealias listener = (String) -> Unit
fun RequestBuilder(url:String, block:RequestBuilder.()->Unit)
        = RequestBuilder(url).apply(block).run{
    Request(url, method, form.takeIf{it.isNotEmpty()}, timeout, ok, fail)
}
class RequestBuilder(private val url:String){
    var method: Method = Method.GET
    val form = mutableMapOf<String, String>()
    fun form(key:String, value:String){this.form[key] = value}
    var timeout = 0
    var ok:listener? = null
    var fail:listener? = null
}

// infix method
// 1
class ListA{
    val list = mutableListOf<String>()
    operator fun get(i:Int) = list[i]
    operator fun plus(b:String) = run {
        list += b
        this
    }
    infix fun add(b:String) = plus(b)
}
//val list = ListA() + "abc"
//println( list[0] )
//println( (list add "def")[1] )

// 2
infix fun <T> T.combine(v:T) = mutableListOf(this, v)
infix fun <T> MutableList<T>.combine(v:T) = run{
    this.add(v)
    this
}
val list = 10 combine 20 combine 30 combine 40
//println("${JSON.stringify(list)}")

// 3
public infix fun <A, B> A.to(that: B): Pair<A, B> = Pair(this, that)
val map = mapOf("a" to 1, "b" to 2)

//constructor

// 1
open public class ClassTest0{
    private val propA:String
    private val propB:String
    public constructor(a:String, b:String){
        println("constructor1")
        propA = a
        propB = b
    }
    public constructor(a:String):this(a, "b"){
        println("constructor2")
    }
}

// 2
class ClassTest0{
    private val propA:String
    private val propB:String
    constructor(a:String, b:String){
        println("constructor1")
        propA = a
        propB = b
    }
    constructor(a:String):this(a, "b"){
        println("constructor2")
    }
}
// 3
class ClassTest0 constructor(a:String, b:String){
    private val propA:String
    private val propB:String
    init{
        println("constructor1")
        propA = a
        propB = b
    }
    constructor(a:String):this(a, "b"){
        println("constructor2")
    }
}
// 4
 class ClassTest0(a:String, b:String){
    private val propA:String
    private val propB:String
    init{
    println("constructor1")
    propA = a
    propB = b
    }
    constructor(a:String):this(a, "b"){
    println("constructor2")
    }
}

// 5
class ClassTest0(private val propA:String, private val propB:String){
    init{
        println("constructor1")
    }
    constructor(a:String):this(a, "b"){
        println("constructor2")
    }
}
// 6
class ClassTest0(private val propA:String, private val propB:String){
    constructor(a:String):this(a, "b")
}

// 7
//open class ClassTest0(private val propA:String, private val propB:String){
//    constructor(a:String):this(a, "b")
//}
//class ClassTest1:ClassTest0("a", "b"){
//    private val propC = "c"
//}
//class ClassTest1(a:String, b:String, c:String):ClassTest0(a, b){
//    private val propC = c
//}
//class ClassTest1(a:String, b:String, private val propC:String):ClassTest0(a, b)

//operator overloading
//simple map
class Map{
    private val map = mutableMapOf<String, String>()
    operator fun get(key:String) = map[key]
    operator fun set(key:String, value:String){map[key] = value}
}
//val m = Map()
////m["test"] = "123"
////println(m["test"])

class Map{
    private val map = mutableMapOf<String, String>()
    operator fun get(key:String) = map[key]
    operator fun set(key:String, value:String){map[key] = value}
    val name:String? get() = map["name"]
    var job:String? get() = map["job"]
        set(value){value?.let{map["job"] = it}}
}
//val m = Map()
//m["name"] = "hika"
//println(m.name)
//m.job = "developer"
//println(m.job)

// by, by lazy
class Map{
    private val map = mutableMapOf<String, String>()
    operator fun get(key:String) = map[key]
    operator fun set(key:String, value:String){map[key] = value}
    val name by lazy{map["firstName"] + " " + map["lastName"]}
}

//html builder
abstract class El(val tagName:String){
    protected val el = when(tagName){
        "body" -> document.body ?: throw Throwable("no body")
        else -> document.createElement(tagName) as HTMLElement
    }
    var html:String get() = el.innerHTML
        set(value){el.innerHTML = value}
    operator fun get(key:String) = el.getAttribute(key) ?: ""
    operator fun set(key:String, value: Any) = el.setAttribute(key, "$value")
    operator fun invoke() = el
    operator fun plusAssign(child:El){el.appendChild(child.el)}
    operator fun minusAssign(child:El){el.removeChild(child.el)}
    val style: CSSStyleDeclaration get() = el.style
}

object Body:El("body")
class Div:El("div")
class Canvas:El("canvas"){
    val context: CanvasRenderingContext2D? get() =
        (el as? HTMLCanvasElement)?.getContext("2d") as? CanvasRenderingContext2D
}

fun htmlBuilder(){
    (0..5).map{Div().apply{html = "div-$it"}}.forEach {Body += it}
    Body += Canvas().apply {
        this["width"] = 500
        this["height"] = 500
        context?.run {
            lineWidth = 10.0
            strokeRect(75.0, 140.0, 150.0, 110.0)
            fillRect(130.0, 190.0, 40.0, 60.0)
            moveTo(50.0, 140.0)
            lineTo(150.0, 60.0)
            lineTo(250.0, 140.0)
            closePath()
            stroke()
        }
    }
}

////fetch builder
//
//class FetchParam{
//    val queries = mutableMapOf<String, Any>()
//    val headers = mutableMapOf<String, String>()
//    var method = "GET"
//}
//fun fetch(url:String, block:FetchParam.()->Unit)= FetchParam().apply{block()}.let{
//    window.fetch(Request(url, RequestInit(
//            method = it.method,
//            headers = run{
//                val obj = js("{}")
//                it.headers.forEach {(k, v)->obj[k] = v}
//                obj
//            },
//            body = if(it.method != "GET") it.queries.toList().joinToString("&"){
//                (k, v)->"$k=$v"
//            }
//            else null
//    )))
//}
//fun testFetch() {
//    fetch("test.json") {}.then {
//        it.text()
//    }.then {
//        println(it)
//    }
//}