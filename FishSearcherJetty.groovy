@Grapes([
	  @Grab('org.eclipse.jetty:jetty-server:9.0.0.M5'),
	  @Grab('org.eclipse.jetty:jetty-servlet:9.0.0.M5'),
	  @Grab('javax.servlet:javax.servlet-api:3.0.1'),
	  @GrabExclude('org.eclipse.jetty.orbit:javax.servlet:3.0.0.v201112011016'),
	  @GrabConfig(systemClassLoader=true)
	])
 
 
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.*
import groovy.servlet.*

s2s2g2val = [:].withDefault { [:].withDefault { new PriorityQueue<Expando>(11, [compare:{a,b-> a.score.compareTo(b.score) }] as Comparator) } }
g2taxon2val = [:].withDefault {  new PriorityQueue<Expando>(11, [compare:{a,b-> a.score.compareTo(b.score) }] as Comparator) }
geno2name = [:]

new File("genotype_features_2015.07.13.txt").splitEachLine("\t") { line ->
  if (! line[0].startsWith("Geno")) {
    def id = line[0]
    def name = line[1]
    def genename = line[8]
    def geneid = line[9]
    Expando exp = new Expando()
    exp.name = name
    exp.id = id
    exp.genename = genename
    exp.geneid = geneid
    geno2name[id] = exp
  }
}

new File("sim_ontology_Differential_phenotype_profiles2/").eachFile { file ->
  file.splitEachLine("\t") { line ->
    def taxa = line[0]?.split("_")
    if (taxa.size() == 2) {
      def geno = line[1]
      def score = new Double(line[4])
      Expando exp = new Expando()
      exp.geno = geno
      exp.score = score
      Expando exp2 = s2s2g2val[taxa[0]][taxa[1]].peek()
      if (exp2 && exp2.score > exp.score) { // do nothing
      } else if (s2s2g2val[taxa[0]][taxa[1]].size() >= 10) {
	s2s2g2val[taxa[0]][taxa[1]].poll()
	s2s2g2val[taxa[0]][taxa[1]].add(exp)
      } else if (s2s2g2val[taxa[1]][taxa[0]].size() >= 10) {
	s2s2g2val[taxa[1]][taxa[0]].poll()
	s2s2g2val[taxa[1]][taxa[0]].add(exp)
      } else {
	s2s2g2val[taxa[0]][taxa[1]].add(exp)
	s2s2g2val[taxa[1]][taxa[0]].add(exp)
      }
      exp = new Expando()
      exp.score = score
      exp.taxon1 = taxa[0]
      exp.taxon2 = taxa[1]
      exp2 = g2taxon2val[geno].peek()
      if (exp2 && exp2.score > exp.score) { // do nothing
	
      } else if (g2taxon2val[geno].size() >= 10) {
	g2taxon2val[geno].poll()
	g2taxon2val[geno].add(exp)
      } else {
	g2taxon2val[geno].add(exp)
      }
    }
  }
}

def startJetty() {
  def server = new Server(30444)
  def context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);

  context.resourceBase = '.'  
  context.addServlet(GroovyServlet, '/QueryFish.groovy')  
  context.setAttribute('version', '1.0')  
  context.setAttribute('map', s2s2g2val)
  context.setAttribute('map2', g2taxon2val)
  context.setAttribute('geno2name', geno2name)
  println "Starting server..."
  server.start()
}
 
startJetty()
