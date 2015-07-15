import groovy.json.*

if (!application) {
  application = request.getApplication(true);
}

def s2s2g2val = application.map
def g2taxon2val = application.map2
def geno2name = application.geno2name

def query = request.getParameter("q")
def querygene = request.getParameter("q2")
def list = request.getParameter("listFish")
def listgenes = request.getParameter("listGeno")

println "<html><body>"

if (list) {
  s2s2g2val.keySet().sort().each { taxon ->
    println """
<a href="http://aber-owl.net:30444/QueryFish.groovy?q=$taxon">$taxon</a><br>
"""
  }
} else if (listgenes) {
  geno2name.values().sort{it.name}.each { gene ->
if (g2taxon2val[gene.id]!=null && g2taxon2val[gene.id].size()>0) {
    println """
    <a href="http://aber-owl.net:30444/QueryFish.groovy?q2=${gene.id}">${gene.name}</a><br>
    """                       
}
}                                                                                                                 
} else if (query){
println """
    <h1>$query</h1>
    <table>
    """
s2s2g2val[query]?.sort().each { taxon, gene2val ->
def first = gene2val.sort {it.score}.reverse()[0]
def name = geno2name[first.geno].name
println """<tr><td><a href="http://aber-owl.net:30444/QueryFish.groovy?q=$taxon"</a>$taxon</td><td><a href="http://aber-owl.net:30444/QueryFish.groovy?q2=${first.geno}"</a><pro>$name</pre></a></td><td>${first.score}</td></tr>"""
} 
//  def builder = new JsonBuilder(s2s2g2val[query])
//  println builder.toString()
println "</table>"
} else if (querygene) {
println """
    <h1>${geno2name[querygene].name}</h1>
    <table>
    """
g2taxon2val[querygene]?.sort{it.score}?.reverse().each { taxon ->
println """<tr><td><a href="http://aber-owl.net:30444/QueryFish.groovy?q=${taxon.taxon1}"</a>${taxon.taxon1}</a></td><td><a href="http://aber-owl.net:30444/QueryFish.groovy?q=${taxon.taxon2}"</a>${taxon.taxon2}</a></td><td>${taxon.score}</td></tr>"""
}
println "</table>"
} else {
  println """
<a href="http://aber-owl.net:30444/QueryFish.groovy?listGeno=1">Browse genotypes</a><br>
<a href="http://aber-owl.net:30444/QueryFish.groovy?listFish=1">Browse taxa</a><br>
"""
}

println "</body></html>"
