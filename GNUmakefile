# HdrHistogram_c makefile
# defines a directory for build, for example, RH6_x86_64
lsb_dist     := $(shell if [ -f /etc/os-release ] ; then \
                  grep '^NAME=' /etc/os-release | sed 's/.*=[\"]*//' | sed 's/[ \"].*//' ; \
                  elif [ -x /usr/bin/lsb_release ] ; then \
                  lsb_release -is ; else echo Linux ; fi)
lsb_dist_ver := $(shell if [ -f /etc/os-release ] ; then \
		  grep '^VERSION=' /etc/os-release | sed 's/.*=[\"]*//' | sed 's/[ \"].*//' ; \
                  elif [ -x /usr/bin/lsb_release ] ; then \
                  lsb_release -rs | sed 's/[.].*//' ; else uname -r | sed 's/[-].*//' ; fi)
#lsb_dist     := $(shell if [ -x /usr/bin/lsb_release ] ; then lsb_release -is ; else uname -s ; fi)
#lsb_dist_ver := $(shell if [ -x /usr/bin/lsb_release ] ; then lsb_release -rs | sed 's/[.].*//' ; else uname -r | sed 's/[-].*//' ; fi)
uname_m      := $(shell uname -m)

short_dist_lc := $(patsubst CentOS,rh,$(patsubst RedHatEnterprise,rh,\
                   $(patsubst RedHat,rh,\
                     $(patsubst Fedora,fc,$(patsubst Ubuntu,ub,\
                       $(patsubst Debian,deb,$(patsubst SUSE,ss,$(lsb_dist))))))))
short_dist    := $(shell echo $(short_dist_lc) | tr a-z A-Z)
pwd           := $(shell pwd)
rpm_os        := $(short_dist_lc)$(lsb_dist_ver).$(uname_m)

# this is where the targets are compiled
build_dir ?= $(short_dist)$(lsb_dist_ver)_$(uname_m)$(port_extra)
bind      := $(build_dir)/bin
libd      := $(build_dir)/lib64
objd      := $(build_dir)/obj
dependd   := $(build_dir)/dep

default_cflags := -ggdb -O3
# use 'make port_extra=-g' for debug build
ifeq (-g,$(findstring -g,$(port_extra)))
  default_cflags := -ggdb
endif
ifeq (-a,$(findstring -a,$(port_extra)))
  default_cflags := -fsanitize=address -ggdb -O3
endif
ifeq (-mingw,$(findstring -mingw,$(port_extra)))
  CC    := /usr/bin/x86_64-w64-mingw32-gcc
  mingw := true
endif
# msys2 using ucrt64
ifeq (MSYS2,$(lsb_dist))
  mingw := true
endif
CC          ?= gcc
cc          := $(CC)
clink       := $(CC)
arch_cflags := -fno-omit-frame-pointer
gcc_wflags  := -Wall -Wno-unknown-pragmas -Wextra -Wshadow -Winit-self -Wpedantic -Wmissing-prototypes
# if windows cross compile
ifeq (true,$(mingw))
sock_lib  := -lws2_32
dll       := dll
exe       := .exe
soflag    := -shared -Wl,--subsystem,windows
fpicflags := -fPIC -DHDR_SHARED
else
thread_lib := -lpthread
dll        := so
exe        :=
soflag     := -shared
fpicflags  := -fPIC
endif
# make apple shared lib
ifeq (Darwin,$(lsb_dist))
dll       := dylib
endif
# rpmbuild uses RPM_OPT_FLAGS
ifeq ($(RPM_OPT_FLAGS),)
CFLAGS ?= $(default_cflags)
else
CFLAGS ?= $(RPM_OPT_FLAGS)
endif
cflags := $(gcc_wflags) $(CFLAGS) $(arch_cflags)

INCLUDES ?= 
includes := -Isrc $(INCLUDES)
DEFINES  ?= 
defines  := -D_GNU_SOURCE $(DEFINES)
rpath    := -Wl,-rpath,$(pwd)/$(libd)
math_lib := -lm

# before include, that has srpm target
.PHONY: everything
everything: all

# copr/fedora build (with version env vars)
# copr uses this to generate a source rpm with the srpm target
-include .copr/Makefile

# debian build (debuild)
# target for building installable deb: dist_dpkg
-include deb/Makefile

# targets filled in below
all_exes    :=
all_libs    :=
all_depends :=
all_dirs    := $(bind) $(libd) $(objd) $(dependd)

libhdrhist_files = hdr_encoding hdr_histogram hdr_histogram_log \
  hdr_interval_recorder hdr_thread hdr_time hdr_writer_reader_phaser
libhdrhist_objs  := $(addprefix $(objd)/, $(addsuffix .o, $(libhdrhist_files)))
libhdrhist_dbjs  := $(addprefix $(objd)/, $(addsuffix .fpic.o, $(libhdrhist_files)))
libhdrhist_deps  := $(addprefix $(dependd)/, $(addsuffix .d, $(libhdrhist_files))) \
                  $(addprefix $(dependd)/, $(addsuffix .fpic.d, $(libhdrhist_files)))
libhdrhist_spec  := $(version)-$(build_num)_$(git_hash)
libhdrhist_ver   := $(major_num).$(minor_num)
libhdrhist_dlnk  := -lz

$(libd)/libhdrhist.a: $(libhdrhist_objs)
$(libd)/libhdrhist.$(dll): $(libhdrhist_dbjs)

all_libs    += $(libd)/libhdrhist.a $(libd)/libhdrhist.$(dll)
all_depends += $(libhdrhist_deps)

gen_files   :=
# the default targets
.PHONY: all
all: $(all_libs) $(all_exes)

# create directories
$(dependd):
	@mkdir -p $(all_dirs)

# remove target bins, objs, depends
.PHONY: clean
clean:
	rm -rf $(bind) $(libd) $(objd) $(dependd)
	if [ "$(build_dir)" != "." ] ; then rmdir $(build_dir) ; fi

.PHONY: clean_dist
clean_dist:
	rm -rf dpkgbuild rpmbuild

.PHONY: clean_all
clean_all: clean clean_dist

# force a remake of depend using 'make -B depend'
.PHONY: depend
depend: $(dependd)/depend.make

$(dependd)/depend.make: $(dependd) $(all_depends)
	@echo "# depend file" > $(dependd)/depend.make
	@cat $(all_depends) >> $(dependd)/depend.make

ifeq (SunOS,$(lsb_dist))
remove_rpath = rpath -r
else
remove_rpath = chrpath -d
endif

# build all, then remove run paths embedded (use /etc/ld.conf.d instead)
.PHONY: dist_bins
dist_bins: all
	$(remove_rpath) $(libd)/libhdrhist.$(dll)

.PHONY: dist_rpm
dist_rpm: srpm
	( cd rpmbuild && rpmbuild --define "-topdir `pwd`" -ba SPECS/hdrhist.spec )

# dependencies made by 'make depend'
-include $(dependd)/depend.make

ifeq ($(DESTDIR),)
# 'sudo make install' puts things in /usr/local/lib, /usr/local/include
install_prefix ?= /usr/local
else
# debuild uses DESTDIR to put things into debian/libdecnumber/usr
install_prefix = $(DESTDIR)/usr
endif
# this should be 64 for rpm based, /64 for SunOS
install_lib_suffix ?=

# create directory structure and copy for lib bin include
install: all
	# install lib, keep symlinks intact
	install -d $(install_prefix)/lib$(install_lib_suffix)
	for f in $(libd)/* ; do \
	if [ -h $$f ] ; then \
	cp -a $$f $(install_prefix)/lib$(install_lib_suffix) ; \
	else \
	install $$f $(install_prefix)/lib$(install_lib_suffix) ; \
	fi ; \
	done
	$(remove_rpath) $(install_prefix)/lib$(install_lib_suffix)/*.$(dll)
	install -d $(install_prefix)/include $(install_prefix)/include/hdrhist
	install -m 644 src/*.h $(install_prefix)/include/hdrhist/

$(objd)/%.o: src/%.c
	$(cc) $(cflags) $(includes) $(defines) $($(notdir $*)_includes) $($(notdir $*)_defines) -c $< -o $@

$(objd)/%.fpic.o: src/%.c
	$(cc) $(cflags) $(fpicflags) $(includes) $(defines) $($(notdir $*)_includes) $($(notdir $*)_defines) -c $< -o $@

$(libd)/%.a:
	ar rc $@ $($(*)_objs)

ifeq (Darwin,$(lsb_dist))
$(libd)/%.dylib:
	$(clink) -dynamiclib $(cflags) -o $@.$($(*)_dylib).dylib -current_version $($(*)_dylib) -compatibility_version $($(*)_ver) $($(*)_dbjs) $($(*)_dlnk) $(sock_lib) $(math_lib) $(thread_lib) $(malloc_lib) $(dynlink_lib) && \
	cd $(libd) && ln -f -s $(@F).$($(*)_dylib).dylib $(@F).$($(*)_ver).dylib && ln -f -s $(@F).$($(*)_ver).dylib $(@F)
else
$(libd)/%.$(dll):
	$(clink) $(soflag) $(rpath) $(cflags) -o $@.$($(*)_spec) -Wl,-soname=$(@F).$($(*)_ver) $($(*)_dbjs) $($(*)_dlnk) $(sock_lib) $(math_lib) $(thread_lib) $(malloc_lib) $(dynlink_lib) && \
	cd $(libd) && ln -f -s $(@F).$($(*)_spec) $(@F).$($(*)_ver) && ln -f -s $(@F).$($(*)_ver) $(@F)
endif

$(bind)/%$(exe):
	$(clink) $(cflags) $(rpath) -o $@ $($(*)_objs) -L$(libd) $($(*)_lnk) $(cpp_lnk) $(sock_lib) $(math_lib) $(thread_lib) $(malloc_lib) $(dynlink_lib)

$(dependd)/%.d: %.c
	$(cc) $(arch_cflags) $(defines) $(includes) $($(notdir $*)_includes) $($(notdir $*)_defines) -MM $< -MT $(objd)/$(*).o -MF $@

$(dependd)/%.fpic.d: %.c
	$(cc) $(arch_cflags) $(defines) $(includes) $($(notdir $*)_includes) $($(notdir $*)_defines) -MM $< -MT $(objd)/$(*).fpic.o -MF $@

