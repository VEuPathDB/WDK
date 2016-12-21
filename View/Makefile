PATH         := $(PATH):$(shell pwd)/node_modules/.bin
SHELL        := /bin/bash

yarn_exec    := $(GUS_HOME)/bin/yarn

# The recipe for this target creates three files, wdk.common.js, wdk.app.js, and wdk.client.js.
# wdk.common.js contains common code between wdk.app.js and wdk.client.js.
wdk_bundle    := $(shell pwd)/dist/wdk/js/wdk.bundle.js
css_bundle    := $(shell pwd)/dist/wdk/css/wdk.min.css

wdk_sources  := $(shell find webapp/wdk/js -name '*.js' -or -name '*.jsx' -or -name '*.ts' -or -name '*.tsx') webpack.config.js
css_sources  := webapp/wdk/css/wdk.css $(shell find webapp/wdk/css -name '*.css')

.PHONY: all clean node_modules

bundles: $(wdk_bundle) $(css_bundle)

$(yarn_exec):
	npm install -g --prefix=$(GUS_HOME) yarn@0.18.0

# =============================================================================
# Install npm packages. This target will prevent unneeded http requests.
# =============================================================================
node_modules: $(yarn_exec)
	yarn


# =============================================================================
# Webpack bundles. See webpack.config.js for details
# =============================================================================

$(wdk_bundle): $(wdk_sources) node_modules
	webpack


# =============================================================================
# Combine CSS files and add source maps
# =============================================================================

$(css_bundle): $(css_sources) node_modules
	mkdir -p $(dir $@)
	cd $(dir $<) && cleancss --skip-rebase --source-map -o $@ $(notdir $<)


# =============================================================================
# Run examples server. See ./client-examples/README.md.
# =============================================================================

examples-server: node_modules
	cd client-examples && webpack-dev-server --content-base public/


# =============================================================================
# Run client tests.
# =============================================================================

tests: node_modules
	tape "tests/**/*.{j,t}s{,x}"


# =============================================================================
# Remove all bundle files
# =============================================================================

clean:
	rm -rf dist
