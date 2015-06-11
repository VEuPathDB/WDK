PATH         := node_modules/.bin:$(PATH)
SHELL        := /bin/bash

# The recipe for this target creates three files, wdk.common.js, wdk.app.js, and wdk.flux.js.
# wdk.common.js contains common code between wdk.app.js and wdk.flux.js.
wdk_bundle    := dist/wdk/js/wdk.common.js
libs_bundle   := dist/wdk/js/wdk.libs.js
css_bundle    := dist/wdk/css/wdk.min.css
test_bundle   := test/dist/tests.js

wdk_sources  := $(shell find webapp/wdk/js -name '*.js') webpack.config.js
libs_sources := webapp/wdk/lib/jquery.js \
                webapp/wdk/lib/jquery-migrate-1.2.1.min.js \
                webapp/wdk/lib/jquery-ui.js \
                webapp/wdk/lib/jquery.cookie.js \
                webapp/wdk/lib/jquery.blockUI.js \
                webapp/wdk/lib/jquery.qtip.min.js \
                webapp/wdk/lib/flexigrid.js \
                webapp/wdk/lib/select2.min.js \
                webapp/wdk/lib/jquery.flot.min.js \
                webapp/wdk/lib/jquery.flot.categories.min.js \
                webapp/wdk/lib/jquery.flot.selection.min.js \
                webapp/wdk/lib/jquery.dataTables.min.js \
                webapp/wdk/lib/dataTables.colVis.min.js \
                webapp/wdk/lib/jstree/jquery.jstree.js \
                webapp/wdk/lib/spin.min.js
css_sources  := webapp/wdk/css/wdk.css
test_sources := test/tests.js $(shell find test/tests -name '*.js')


.PHONY: all clean test test-server

all: $(wdk_bundle) $(wdk3_bundle) $(libs_bundle) $(css_bundle)


# =============================================================================
# Webpack bundles. See webpack.config.js for details
# =============================================================================

$(wdk_bundle): $(wdk_sources)
	webpack

# =============================================================================
# Concatenate libs into a single file, separated by newline
# =============================================================================

$(libs_bundle): $(libs_sources)
	mkdir -p $(dir $@)
	awk 'FNR==1{print ""}1' $^ > $@


# =============================================================================
# Combine CSS files and add source maps
# =============================================================================

$(css_bundle): $(css_sources)
	mkdir -p $(dir $@)
	cleancss --source-map --skip-rebase -o $@ $^


# =============================================================================
# Create test bundles. See webpack-test.config.js
# =============================================================================

$(test_bundle): $(test_sources) $(wdk_sources)
	webpack --config webpack-test.config.js $< $@

test: $(test_bundle) $(libs_bundle)
	cp $(libs_bundle) test/dist


# =============================================================================
# Run test server, which recompiles the test bundle on-the-fly
# =============================================================================

test-server: $(test_sources) $(wdk_sources) $(libs_bundle)
	webpack-dev-server --config webpack-test.config.js $<
	cp $(libs_bundle) test/dist


# =============================================================================
# Remove all bundle files
# =============================================================================

clean:
	rm -rf dist
