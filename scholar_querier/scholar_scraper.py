# Script that takes a string representing the SEARCH TERMS and generates a PaperEntry structure for the first result 
# that comes up when these terms are searched in google scholar. 

# The PaperEntry structure will be exported into JSON where it will then be used by our frontend to update the visual graph
# and in our backend to persist the data to our SQL data store 

from bs4 import BeautifulSoup
import requests
import bibtexparser
from time import sleep

# TODO: Experiment to see exactly what the boundaries are for flying under the radar of scholar bot detection
# TODO: should we extract the MLA citation or the BibTex? BibTex would be easier to parse, but possibly more automated requests
# TODO: We should design this keeping FLEXIBILITY in mind, theres a lot more info that can be extracted from the search results page  

DEBUG = 1
REQUEST_DELAY = 5 #set to the ridiculously high time of 5 seconds for testing purposes PLZ DONT BAN ME GOOGLE !!!

# class that is used to collect the data that will be packed into the JSON file
# TODO: Is tohis it??? One thing we might add is a short abstract that is in the short preview in GS (it trails off with the ellipses), since we already have it scraped.
class PaperEntry():
	def __init__(self, authors_, title_, journal_, year_, source_url_, cited_by_list_):
		self.authors       = authors_
		self.title         = title_
		self.journal       = journal_
		self.year          = year_
		self.source_url    = source_url_
		self.cited_by_list = cited_by_list_
		return

# genertates a google scholar SEARCH REQUEST URL from a search input
# TODO: Will URL info channge based on someonse date/time/location/browser/etc ?
def scholar_url_maker(srch_str):
	terms_str = srch_str.replace(" ", "+")
	before_terms = "https://scholar.google.com/scholar?hl=en&as_sdt=0%2C43&q="
	after_terms = "&btnG="

	req_url = before_terms + terms_str + after_terms

	return req_url

# takes a URL as input, returns a beautiful soup data struct for that page
def delayed_request(url):
	# TODO: introduce randomness to delay?
	sleep(REQUEST_DELAY)
	raw_html = requests.get(url).text
	result_soup = BeautifulSoup(raw_html, 'lxml')
	return result_soup

# TODO: will we use bibtex? It means more url requests (making users look more like bots), but will be easier to parse)
# extracts the bibtex info from the first result in the html, parses it into a class that will be used to generate our json file
def extract_bibtex(entry_div):
	return

# takes <div> section for the paper entry as input and returns the url fro the MLA citation
def extract_citation_url(entry_div):
	# EXTRACTNG THE MLA/APA/BIBTEX CITATION LINK
	# Default:
	#	https://scholar.google.com/scholar?q=info:      {id}      :scholar.google.com/&output=cite&scirp={p}&hl=en
	# Turns into:
	#	https://scholar.google.com/scholar?q=info:  f0Kgtf5gNsgJ  :scholar.google.com/&output=cite&scirp={p}&hl=en
	# where {id} is replaced with the "data-cid" in the TAGS of the <div> for that specific search result entry:
	# 	<div class="gs_r gs_or gs_scl" data-cid="f0Kgtf5gNsgJ" data-did="f0Kgtf5gNsgJ" data-lid data-rp="0">…</div>
	#   We might be able to use these "c-ids" in our own SQL table, but we should think about it a bit more carefully
	cit_url_start = "https://scholar.google.com/scholar?q=info:"
	cit_url_end   = ":scholar.google.com/&output=cite&scirp={p}&hl=en"

	entry_cid = entry_div.get('data-cid')
	citation_url = cit_url_start + entry_cid + cit_url_end
	return citation_url

# take a paper's <div> soup entry and return the MLA text string
def extract_citation(entry_div):
	citation_url = extract_citation_url(entry_div)
	# Find the first <div tabindex="0" class="gs_citr"> within the page's html
	citation_soup = delayed_request(citation_url)

	mla_soup = citation_soup.find('div', class_='gs_r gs_or gs_scl')
	mla_text = mla_soup.text
	
	return mla_text

# take the <div> section for the paper entry as input and populate a PaperEntry object with the info
def parse_scholar_entry(entry_div): 
	
	return

# Takes a user search string and searches for it on google scholar
# Extracts the raw HTML from the search result page
# Extracts the entire <div></div> for the FIRST result
# Passes ALL that raw info to parse_scholar_entry(), where the info is returned packaged in our custom "PaperEntry" Class
# Pass that PaperEntry object back to the caller of this function
def scholar_search(srch_str):
	search_url = scholar_url_maker(srch_str)
	search_soup = delayed_request(search_url)

	first_result_entry = search_soup.find('div', class_='gs_r gs_or gs_scl')

	if DEBUG:
		print("FIRST RESULT: \n\n", search_soup.prettify(), "\n")

	# TODO: handle a search that has no results more gracefully (what does the html of a "nothing found" or "WE FOUND UR BOT GET REKT" page look like?)
	assert first_result_entry != None

	# construct the citation URL, which is made from a section appended from the first result entry
	# the citation URL is in the <div> we extracted and looks like this for the first entry in "network science borner":
	# 	<a href="/scholar?cites=14426825103413101183&as_sdt=5,43&sciodt=0,43&hl=en">Cited by 388</a>
	# print(citation_url, "\n")
	cited_by_url = ""
	for curr_a_tag in first_result_entry.find_all('a'):
		url = curr_a_tag.get('href')
		url_pieces = url.split('=')
		if url_pieces[0] == "/scholar?cites":
			cited_by_url = "https://scholar.google.com" + url
			break
	
	print("CITED BY URL: ", cited_by_url)

	# Extract the bibliographic info from each of the papers that cite this one. Space requests out by 100 ms. 
	# Each page in the "cited by" results contians 10 entries. This tag:
	# 	start=10&hl=en&as_sdt=5,43&sciodt=0,43&
	# denotes the start point of the results on each page
	curr_page = 0
	cited_by_paper_entries = []
	
	while cited_by_url != "":
		
		index = cited_by_url.find('?')
		curr_page_url = cited_by_url[:(index + 1)] + "start=" + str(curr_page * 10) + "&hl=en&as_sdt=5,43&sciodt=0,43&" + cited_by_url[(index + 1):]
		cited_by_soup = delayed_request(curr_page_url)
		
		for curr_entry in cited_by_soup.find_all('div', class_="gs_r gs_or gs_scl"):
			mla_text = extract_citation(curr_entry)
			print(mla_text)
			#print(curr_entry.prettify(), "\n")
			#curr_parsed_entry = parse_scholar_entry(entry_div)
			#cited_by_paper_entries.append(curr_parsed_entry)
		curr_page += 1
		
		# XXX protects from over-requesting
		if curr_page > 2:
			cited_by_url = ""

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
	#   ERRYTHANG ELSE

	# TODO: keep in mind not every paper will have papers that cite it. Test those kinds of edge cases!!!

	return

def main():
	return

if DEBUG:
	tests()

#main()