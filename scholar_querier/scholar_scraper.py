# Script that takes a string representing the SEARCH TERMS and generates a PaperEntry structure for the first result 
# that comes up when these terms are searched in google scholar. 

# The PaperEntry structure will be exported into JSON where it will then be used by our frontend to update the visual graph
# and in our backend to persist the data to our SQL data store 

from bs4 import BeautifulSoup
import requests
import random
import json
#import bibtexparser
from time import sleep

# TODO: Experiment to see exactly what the boundaries are for flying under the radar of scholar bot detection
# TODO: should we extract the MLA citation or the BibTex? BibTex would be easier to parse, but possibly more automated requests
# TODO: We should design this keeping FLEXIBILITY in mind, theres a lot more info that can be extracted from the search results page  
# TODO: Put in some "try, except" statements so this program can fail gracefully if data doesn't align perfectly

DEBUG = 1
REQUEST_DELAY = 5 #set to the ridiculously high time of 5 seconds for testing purposes PLZ DONT BAN ME GOOGLE !!!
DIV_ENTRY_CLASS = 'gs_r gs_or gs_scl'

# generates a google scholar SEARCH REQUEST URL from a search input
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
    sleep( REQUEST_DELAY + (1 + random.random()) )
    raw_html = requests.get(url).text
    result_soup = BeautifulSoup(raw_html, 'lxml')
    return result_soup

# takes a paper entry div as input and returns the info that can be gathered WITHOUT issuing out more requests
def get_basic_info(entry_div):
    return

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

class ShortPaperInfo():
    def cache_citing_papers(self, page_no):
        # Avoids pages with no results
        if page_no * 10 > self.cited_by_count:
            return

        index = self.cited_by_url.find('?')
        citing_page_url = self.cited_by_url[:(index + 1)] + "start=" + str(page_no * 10) + "&hl=en&as_sdt=5,43&sciodt=0,43&" + self.cited_by_url[(index + 1):]
        cited_by_soup = delayed_request(citing_page_url)
        
        papers_on_req_page = []
        for curr_div_entry in cited_by_soup.find_all('div', class_='gs_r gs_or gs_scl'):
            papers_on_req_page.append(ShortPaperInfo(curr_div_entry))
        
        # Merge result list of retrieved objects with current list, no duplicates
        self.referenced_by = list(set(self.referenced_by + papers_on_req_page))

    def authordict_and_year(self, entry_div):
        # TODO: properly parse the line
        # 	<div class="gs_a"><a href="https://scholar.google.com/citations?user=YirSp_cAAAAJ&amp;hl=en&amp;oi=sra">K <b>Börner</b></a>, S Sanyal, <a href="https://scholar.google.com/citations?user=U3CXAPsAAAAJ&amp;hl=en&amp;oi=sra">A Vespignani</a>&nbsp;- …&nbsp;of information <b>science </b>and&nbsp;…, 2007 - Wiley Online Library</div>
        subsection = entry_div.find('div', class_='gs_a')
        
        author_dict = {}
        for auth_a_tags in subsection.find_all('a'):
            author_dict[auth_a_tags.text] = "https://scholar.google.com" + auth_a_tags.get('href')
        
        # extract the year
        raw_string = str(subsection)
        raw_string = raw_string[raw_string.rfind(',') + 2:]
        raw_string = raw_string[:raw_string.rfind('-') - 1]
        year = int(raw_string)

        return author_dict, year
    def cited_by_url_and_count(self, entry_div):
        # construct the citation URL, which is made from a section appended from the first result entry
        # the citation URL is in the <div> we extracted and looks like this for the first entry in "network science borner":
        # 	<a href="/scholar?cites=14426825103413101183&as_sdt=5,43&sciodt=0,43&hl=en">Cited by 388</a>
        # print(citation_url, "\n")

        cited_by_url = ""
        cited_by_count = 0

        for curr_a_tag in entry_div.find_all('a'):
            url = curr_a_tag.get('href')
            url_pieces = url.split('=')
            if url_pieces[0] == "/scholar?cites":
                cited_by_url   = "https://scholar.google.com" + url
                count_text = curr_a_tag.text
                cited_by_count = count_text.split()[2]
                break	
        return cited_by_url, int(cited_by_count)
    def get_source_url_and_id(self, div_entry):
        for curr_a_tag in div_entry.find_all('a'):
            if curr_a_tag.get('data-clk-atid') != None:
                return curr_a_tag.get('href'), curr_a_tag.get('id')
        return None
    def __init__(self, entry_div):
        # TODO: Extract the links to the authors' own pages !!!
        # authors_and_links Dictionary -> Key: Author Name, Value: Link to that author's scholar page 
        self.authors_and_links, self.year      = self.authordict_and_year(entry_div)
        self.title_short     		           = entry_div.find('h3', class_='gs_rt').a.text
        self.summary_short                     = entry_div.find('div', class_='gs_rs').text
        self.source_url, self.scholar_id	   = self.get_source_url_and_id(entry_div)
        self.cited_by_url, self.cited_by_count = self.cited_by_url_and_count(entry_div)
        
        if self.cited_by_count == 0:
            self.referenced_by = ["NoneExist"]
        else:
            self.referenced_by = []

        if entry_div.find('div', class_='gs_or_ggsm') == None:
            self.doc_url = ""
        else:
            self.doc_url = entry_div.find('div', class_='gs_or_ggsm').a.get('href')

        return
    def __str__(self):
        output = self.title_short + ": \n"
        for attribute in self.__dict__:
            if type(self.__dict__[attribute]) is not list:
                output += "   " + str(attribute) + ": " + str(self.__dict__[attribute]) + "\n"
        if len(self.referenced_by) > 0:
            output += "\nREFERENCED BY:\n\n"
        for item in self.referenced_by:
            output += str(item) + "\n"
        return output
    def __eq__(self, other):
        if type(other) is ShortPaperInfo:
            if other.titleShort == self.title_short and \
                other.authors_and_links == self.authors_and_links and \
                other.year == self.year:
                return True
        return False
    def __hash__(self):
        return hash((self.scholar_id, self.title_short))
    def to_json_file(self, file_name):
        if file_name.lower().find(".json") == -1:
            file_name += ".json"
        with open(file_name, 'w') as outfile:
            json.dump(self, outfile, sort_keys=True, indent=4, default=vars)



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

    # TODO: This block of code used to extract every citation from each entry of each page of the "cited_by" page for apaper
    #       commented out for now, because it was getting me banned from scholar :(
    # # Extract the bibliographic info from each of the papers that cite this one. Space requests out by 100 ms. 
    # # Each page in the "cited by" results contians 10 entries. This tag:
    # # 	start=10&hl=en&as_sdt=5,43&sciodt=0,43&
    # # denotes the start point of the results on each page
    # curr_page = 0
    # cited_by_paper_entries = []
    # while cited_by_url != "":
    # 	index = cited_by_url.find('?')
    # 	curr_page_url = cited_by_url[:(index + 1)] + "start=" + str(curr_page * 10) + "&hl=en&as_sdt=5,43&sciodt=0,43&" + cited_by_url[(index + 1):]
    # 	cited_by_soup = delayed_request(curr_page_url)
    # 	for curr_entry in cited_by_soup.find_all('div', class_="gs_r gs_or gs_scl"):
    # 		mla_text = extract_citation(curr_entry)
    # 		print(mla_text)
    # 		#print(curr_entry.prettify(), "\n")
    # 		#curr_parsed_entry = parse_scholar_entry(entry_div)
    # 		#cited_by_paper_entries.append(curr_parsed_entry)
    # 	curr_page += 1
    # 	# XXX protects from over-requesting
    # 	if curr_page > 2:
    # 		cited_by_url = ""

    # Grabs the basic info for this paper

    # get BASIC INFO from the first result itself
    first_result_basic = ShortPaperInfo(first_result_entry)

    # TODO TODO: Grab the BASIC INFO from each paper in the FIRST PAGE of "cited_by" results
    return first_result_basic

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
    
    # Scholar search just returns the short info right now
    search_result = scholar_search(search_url)
    search_result.cache_citing_papers(0)
    print(search_result)
    print("\nsearch_result.authors_and_links['K Börner'] == ", search_result.authors_and_links['K Börner'], "\n")
    assert search_result.authors_and_links['K Börner'] == "https://scholar.google.com/citations?user=YirSp_cAAAAJ&hl=en&oe=ASCII&oi=sra"

    # save ShortPaper class as JSON
    search_result.to_json_file("output")

    # TODO: re-open JSON and check that data is accessible, saved correctly

    # TODO: write hard-coded tests for:
    # 	scholar_search()
    #   parse_scholar_entry()
    # 	extract_bibtex(raw_html)
    # 	json_export(paper_entry)
    #   ERRYTHANG ELSE

    # TODO: keep in mind not every paper will have papers that cite it. Test those kinds of edge cases!!!

    return

if DEBUG:
    tests()

def main():
    while True:
        user_terms = user_terms = input("Enter some search terms: ")
        if user_terms == "fuck off":
            break
        search_result = scholar_search(user_terms)
        search_result.cache_citing_papers(0)
        print("\n", search_result)
        
    return

if DEBUG != 1:
    main()