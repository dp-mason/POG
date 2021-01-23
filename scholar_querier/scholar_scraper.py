from bs4 import BeautifulSoup
import requests
import bibtexparser

# TODO: should we extract the MLA citation or the BibTex? BibTex would be easier to parse, but possibly more automated requests
# TODO: We should design this keeping FLEXIBILITY in mind, theres a lot more info that can be extracted from the search results page  

DEBUG = 1

# class that is used to collect the data that will be packed into the JSON file
# TODO: Is tohis it??? One thing we might add is a short abstract that is in the short preview in GS (it trails off with the ellipses), since we already have it scraped.
class PaperEntry():
	def __init__(authors_, title_, journal_, year_, source_url_, cited_by_list_):
		self.authors       = authors_
		self.title         = title_
		self.journal       = journal_
		self.year          = year_
		self.source_url    = source_url_
		self.cited_by_list = cited_by_list_

# genertates a google scholar search request url from a search input
# TODO: Will URL info channge based on someonse date/time/location/browser/etc ?
def scholar_url_maker(srch_str):
	terms_str = srch_str.replace(" ", "+")
	before_terms = "https://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q="
	after_terms = "&btnG="

	req_url = before_terms + terms_str + after_terms

	return req_url

# TODO: will we use bibtex? It means more url requests (making users look more like bots), but will be easier to parse)
# extracts the bibtex info from the first result in the html, parses it into a class that will be used to generate our json file
def extract_bibtex(entry_div):
	return

def parse_scholar_entry(entry_div): 
	# EXTRACTNG THE MLA/APA/BIBTEX CITATION LINK
	# Default:
	#	https://scholar.google.com/scholar?q=info:      {id}      :scholar.google.com/&output=cite&scirp={p}&hl=en
	# Turns into:
	#	https://scholar.google.com/scholar?q=info:  f0Kgtf5gNsgJ  :scholar.google.com/&output=cite&scirp={p}&hl=en
	# where {id} is replaced with the "data-cid" in the TAGS of the <div> for that specific search result entry:
	# 	<div class="gs_r gs_or gs_scl" data-cid="f0Kgtf5gNsgJ" data-did="f0Kgtf5gNsgJ" data-lid data-rp="0">…</div>
	#   We might be able to use these "c-ids" in our own SQL table, but we should think about it a bit more carefully
	return


# Takes a user search string and searches for it on google scholar
# Extracts the raw HTML from the search result page
# Extracts the entire <div></div> for the FIRST result
# Passes ALL that raw info to parse_scholar_entry(), where the info is returned packaged in our custom "PaperEntry" Class
# Pass that PaperEntry object back to the caller of this function
def scholar_search(srch_str):
	search_url = scholar_url_maker(srch_str)
	raw_html = requests.get(search_url).text
	search_soup = BeautifulSoup(raw_html, 'lxml')

	first_result_entry = search_soup.find('div', class_='gs_r gs_or gs_scl')

	# TODO: handle a search that has no results more gracefully (what does the html of a "nothing found" page look like?)
	assert first_result_entry != None

	print(first_result_entry.prettify(), "\n")

	# construct the citation URL, which is made from a section appended from the first result entry
	# the citation URL is in the <div> we extracted and looks like this for the first entry in "network science borner":
	# 	<a href="/scholar?cites=14426825103413101183&as_sdt=5,43&sciodt=0,43&hl=en">Cited by 388</a>
	# print(citation_url, "\n")

	return ""


# exports the data in the class we have filled out as a JSON file using the (Jackson JSON library?)
def json_export(paper_entry):
	return

def tests():
	search_string = "elephants"
	search_url = scholar_url_maker(search_string)
	if DEBUG:
		print("tests: \n", search_url, "\n")
	assert search_url == "https://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q=elephants&btnG="

	search_string = "network science borner"
	search_url = scholar_url_maker(search_string)
	if DEBUG:
		print("tests: search str == ", search_string, " url == \n", search_url, "\n")
	assert search_url == "https://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q=network+science+borner&btnG="
	
	# XXX: scholar search just prints out diagnostic stuff right now
	scholar_search(search_url)
	
	# TODO: write hard-coded tests for:
	# 	scholar_search()
	#   parse_scholar_entry()
	# 	extract_bibtex(raw_html)
	# 	json_export(paper_entry)

	# TODO: keep in mind not every paper will have papers that cite it. Test those kinds of edge cases!!!

	return

def main():
	return
	
tests()