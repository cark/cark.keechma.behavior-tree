rd /S /Q gh-pages
md gh-pages
md gh-pages\js
call shadow-cljs release example
copy resources\public\example\index.html gh-pages
copy resources\public\example\js\main.js gh-pages\js
git checkout gh-pages
copy /Y gh-pages\index.html .
copy /Y ghpages\js\main.js .\js
