@Grab(group='org.semanticweb.elk', module='elk-owlapi-standalone', version='0.4.2')

import java.util.logging.Logger
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.owlapi.profiles.*
import org.semanticweb.owlapi.util.*
import org.semanticweb.owlapi.io.*
import org.semanticweb.elk.owlapi.*
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

OWLDataFactory fac = manager.getOWLDataFactory()
OWLDataFactory factory = fac

println "Loading ontology file..."
OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("monarch/monarch-inferred.owl"))
println "Ontology file loaded..."

OWLReasonerFactory reasonerFactory = null

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)

println "Classifying ontology..."
OWLReasonerFactory f1 = new ElkReasonerFactory()
OWLReasoner reasoner = f1.createReasoner(ont,config)
println "Ontology classified..."

def r = { String s ->
  if (s == "part-of") {
    factory.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000050"))
  } else if (s == "has-part") {
    factory.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000051"))
  } else if (s == "inheres-in") {
    factory.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0000052"))
  } else {
    factory.getOWLObjectProperty(IRI.create("http://phenomebrowser.net/#"+s))
  }
}

def c = { String s ->
  factory.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/"+s))
}

println "Processing file..."
new File(args[0]).splitEachLine("\t") { line ->
  if (!line[0].startsWith("Taxon 1")) {
    def e1 = line[2]?.replaceAll(":","_").split(" ")[0]
    def e2 = line[3]?.replaceAll(":","_").split(" ")[0]
    def p = line[4]?.replaceAll(":","_")
    def cl = fac.getOWLObjectSomeValuesFrom(r("has-part"),
				      fac.getOWLObjectIntersectionOf(c(p), fac.getOWLObjectSomeValuesFrom(r("inheres-in"),c(e1))))
    reasoner.getSuperClasses(cl, true).each { sup ->
      println sup
    }
  }
}
