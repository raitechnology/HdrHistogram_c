Name:		hdrhist
Version:	999.999
Release:	99999%{?dist}
Summary:	High Dynamic Range (HDR) Histogram

License:	BSD
URL:		https://github.com/HdrHistogram/HdrHistogram_c
Source0:	%{name}-%{version}-99999.tar.gz
BuildRoot:	${_tmppath}
Prefix:	        /usr
BuildRequires:  gcc-c++
BuildRequires:  zlib-devel
BuildRequires:  chrpath
Requires:       zlib
Requires(post): /sbin/ldconfig
Requires(postun): /sbin/ldconfig

%description
This 'C' port of HDR contains a subset of the functionality supported by the
Java implementation.

%prep
%setup -q


%define _unpackaged_files_terminate_build 0
%define _missing_doc_files_terminate_build 0
%define _missing_build_ids_terminate_build 0
%define _include_gdb_index 1

%build
make build_dir=./usr %{?_smp_mflags} dist_bins
install -d ./usr/include
install -d ./usr/include/hdrhist
install -m 644 src/*.h ./usr/include/hdrhist/

install -d ./usr/share/doc/%{name}
install -m 644 README.md LICENSE.txt COPYING.txt ./usr/share/doc/%{name}/

%install
rm -rf %{buildroot}
mkdir -p  %{buildroot}

# in builddir
cp -a * %{buildroot}

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
/usr/lib64/*
/usr/include/*
/usr/share/doc/*

%post
echo "${RPM_INSTALL_PREFIX}/lib64" > /etc/ld.so.conf.d/%{name}.conf
/sbin/ldconfig

%postun
# if uninstalling
if [ $1 -eq 0 ] ; then
  rm -f /etc/ld.so.conf.d/%{name}.conf
fi
/sbin/ldconfig

%changelog
* __DATE__ <gchrisanderson@gmail.com>
- Hello world
