import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.KeyboardEvent
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
        val method:Method,
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