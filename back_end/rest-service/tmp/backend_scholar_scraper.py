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
import re
import os
import time

# TODO: Experiment to see exactly what the boundaries are for flying under the radar of scholar bot detection
# TODO: should we extract the MLA citation or the BibTex? BibTex would be easier to parse, but possibly more automated requests
# TODO: We should design this keeping FLEXIBILITY in mind, theres a lot more info that can be extracted from the search results page  
# TODO: Put in some "try, except" statements so this program can fail gracefully if data doesn't align perfectly

DEBUG = 0
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
        # extract all numbers from this string
        numbers = re.findall('[0-9]+', raw_string)
        if(len(numbers) > 1):
            print("ShortPaperInfo.authordict_and_year(): one of these numbers is not the year, check to make sure everything works")

        if(len(numbers) < 1):
            print("no year found for ", self.title_short)
            year = 0000
        else:
            year = int(numbers[-1])

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
                return curr_a_tag.get('href'), curr_a_tag.get('data-clk-atid')
        return None
    def __init__(self, entry_div):
        # TODO: Extract the links to the authors' own pages !!!
        # authors_and_links Dictionary -> Key: Author Name, Value: Link to that author's scholar page
        if entry_div.find('h3', class_='gs_rt').a == None:
            return None
        self.title_short     		       = entry_div.find('h3', class_='gs_rt').a.text            
        self.authors_and_links, self.year      = self.authordict_and_year(entry_div)
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
    #TODO: DELETE THIS
    #def to_json_file(self, file_name):
    #    if file_name.lower().find(".json") == -1:
    #        file_name += ".json"
    #    with open(file_name, 'w') as outfile:
    #        json.dump(self, outfile, sort_keys=True, indent=4, default=vars)

class ParsedPageInfo():
    def find_papers_in(self, raw_page_html):
        found_papers = []
        
        soup = BeautifulSoup(raw_page_html, 'lxml')
        paper_sections_on_this_page = soup.find_all('div', class_='gs_r gs_or gs_scl')
        
        # parse each of the raw sections found
        for raw_section in paper_sections_on_this_page:
            if DEBUG:
                print("\n\n", raw_section.prettify(), "\n\n")
            parsed_paper_info = ShortPaperInfo(raw_section)
            if parsed_paper_info != None:
                found_papers.append(parsed_paper_info)
        
        return found_papers
    
    def __init__(self, id, entire_scholar_page_html):
        # paper id that is the paper that all of the papers on this page cite
        self.id = id
        # info about each of the papers on this page
        self.papers = self.find_papers_in(entire_scholar_page_html)
    
    def to_json_file(self, file_name):
        if file_name.lower().find(".json") == -1:
            file_name += ".json"
        with open(file_name, 'w') as outfile:
            json.dump(self, outfile, sort_keys=True, indent=4, default=vars)

# TODO: this and all other standalone functions can be removed from the backend parser, they are not used
# Takes a user search string and searches for it on google scholar
# Extracts the raw HTML from the search result page
# Extracts the entire <div></div> for the FIRST result
# Passes ALL that raw info to parse_scholar_entry(), where the info is returned packaged in our custom "PaperEntry" Class
# Pass that PaperEntry object back to the caller of this function
def scholar_search(srch_str):
    search_url = scholar_url_maker(srch_str)
    search_soup = delayed_request(search_url)

    result_entries = search_soup.find_all('div', class_='gs_r gs_or gs_scl')

    # Printing the HTML will expose whether issues arise bc of a scholar block
    if DEBUG:
        print("FIRST RESULTS: \n\n", search_soup.prettify(), "\n")

    # TODO: handle a search that has no results more gracefully (what does the html of a "nothing found" or "WE FOUND UR BOT GET REKT" page look like?)
    assert result_entries != []

    # Grabs the basic info for the first 10 papers

    # get BASIC INFO from the first result itself
    first_ten_results = []
    for entry in result_entries:
        print("\n\n", entry.prettify(), "\n\n")
        parsed_entry = ShortPaperInfo(entry)
        if parsed_entry != None:
            first_ten_results.append(ShortPaperInfo(entry))

    # TODO TODO: Grab the BASIC INFO from each paper in the FIRST PAGE of "cited_by" results
    return first_ten_results

def stand_alone_tests():
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
    search_results = scholar_search(search_url)
    search_results[0].cache_citing_papers(0)
    print(search_results[0])
    print("\nsearch_result.authors_and_links['K Börner'] == ", search_results[0].authors_and_links['K Börner'], "\n")
    
    assert search_results[0].authors_and_links['K Börner'] == "https://scholar.google.com/citations?user=YirSp_cAAAAJ&hl=en&oe=ASCII&oi=sra"
    assert search_results[0].scholar_id == "f0Kgtf5gNsgJ"

    # save ShortPaper class as JSON
    search_results[0].to_json_file("output")

    # TODO: re-open JSON and check that data is accessible, saved correctly

    # TODO: write hard-coded tests for:
    # 	scholar_search()
    #   parse_scholar_entry()
    # 	extract_bibtex(raw_html)
    # 	json_export(paper_entry)
    #   ERRYTHANG ELSE

    # TODO: keep in mind not every paper will have papers that cite it. Test those kinds of edge cases!!!
    while True:
        print("\n\nTESTS WITH YOUR INPUT:\n\n")
        user_terms = user_terms = input("Enter some search terms: ")
        if user_terms == "fuck off":
            break
        usr_search_results = scholar_search(user_terms)
        usr_search_results[0].cache_citing_papers(0)
        print("\n", usr_search_results[0])
        
    return

def backend_tests():
    file_name = "raw_html.html"
    input_file = open(file_name, "r", encoding='utf-8')
    raw_html = input_file.read()
    parsed_info = ParsedPageInfo(file_name[0:-5], raw_html)
    parsed_info.to_json_file("backend_output.json")
    # delete the html input file
    if os.path.exists(file_name):
        os.remove(file_name)
    else:
        print("The file " + file_name + "does not exist")

if DEBUG:
    backend_tests()

def main():
    # Pseudocode for final real functionality:
    # WAIT LOOP ON EVENT
        # Open HTML file
        # Parse results found in the HTML opened
        # Create a JSON that will be sent back to the REST API

    curr_workdir = os.getcwd()

    for filename in os.listdir(curr_workdir):
        if filename.endswith(".html"):
            id = filename[0:-5] # cutout the ".html" extension from id
            outfile_name = ""
            
            # prototype for the stand-in "id" we might give to html pages that are search result pages and aren't "cited by" pages
            outfile_name = id + ".json"

            while not os.path.exists(filename):
                time.sleep(1)

            if not os.path.isfile(filename):
                raise ValueError("%s isn't a file!" % filename)

            input_file = open(filename, "r", encoding='utf-8')
            raw_html = input_file.read()
            parsed_info = ParsedPageInfo(filename[0:-5], raw_html)
            parsed_info.to_json_file(outfile_name)
            
            # delete the html input file that was just parsed
            if os.path.exists(filename):
                os.remove(filename)
            else:
                print("The file " + filename + "does not exist")
                
            # TODO: how does python interface with SQL
            # Make an entry into the SQL table for the parsed papers, make info updates in table if necessary
            return

if DEBUG != 1:
    main()