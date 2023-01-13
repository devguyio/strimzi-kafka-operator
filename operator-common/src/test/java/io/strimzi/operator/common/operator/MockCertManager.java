/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.operator.common.operator;

import io.strimzi.certs.CertManager;
import io.strimzi.certs.Subject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;

public class MockCertManager implements CertManager {

    private static final String CLUSTER_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDbFnJj90sKoM35\n" +
            "VszJsfwNvO5dshoeFIb2idf7h+l0h3GMv29j+1XtmLJGzxiYy320KFZr3IKWbq+D\n" +
            "abqdlqEqZm9NZ1Kq9d7mB10zulQce5JwVZ3FqpCmLku2jHCaDXzTKC3T/Xp0O9Oe\n" +
            "8+42ysSMCTd8p8aZ4vAyJMCKcoyVCGHrUWVba40D7cQNOlhJplSzHZdLFYZ13kwz\n" +
            "pT5GpDEPhGVmtF8qV918lSxvdpuepyeFdOSYY88FEMMLLrlZG4QCPyES4FpcUXMz\n" +
            "zvZeLIlZnKNIYbao3Kx+yZv//wjC80/pqdyoZ5+K5hDxjby2+f+2dh0TadKRZC2p\n" +
            "p+j3/z63AgMBAAECggEBAJN5iai25uGRmvSzNAi08VkCC2YwpBoJcUv1P9jGBST2\n" +
            "oz2+AypHHfFgruixMPpxR/2EhZ/3gEPo3+ZSvlaj9XrIFzYATgpclR08acWPMF03\n" +
            "5TwOtbQ/+zyRv09zO7zHRXYR/r9LSimBuBKwWnKxjRpCfgJAIZSmyU7HpH/NWcpa\n" +
            "6khQen5zQVsjnlv3L5OSN8exP8okVYY01JmrKTyo3IWsOiTogr1ebe84xvG2QzkV\n" +
            "fdL99poVfXXSBivLwgGPiCNkZFtwcjDRECzBHAcrwYT1AIHsvsWvdbrXilAh/388\n" +
            "1v3HWw7lfnJi8WV9iAplq1YOEwqKN6z5Y/wwAZQafHECgYEA8F3lGtOUIMeaTXeI\n" +
            "9xlXAM1MXQPPxguMFS9ilcnqlTdNJjJ18FBWM/Kn73RF9mTCQCYn+5o6MmoyMQIq\n" +
            "rreBchP7Bzg7RCKVvPnippqwVDeLY7khdA0I1lH6mD7urhBvO5Fby3wKZeJfi2N9\n" +
            "suQzEuRDEw5EDHQf/0rP4RBnrg8CgYEA6VZBUKuur885Q/knRUKdIG/mXYAcvmAV\n" +
            "I6kjFCVRx7lQ8xLTiagKo0SKr0TM0Qf1+4vqTUZCOBtGlW/UBdGW8yPvXegyMHQg\n" +
            "pMoNCTnxgXS77f4pnluQRQux8M8oWJnf11oGxDHhLw0T7kqBHl9K/dw/cItRS5Ui\n" +
            "Dch/2YDMDNkCgYBNqZjXxRrsSHHTq9amOBrDWJHez9d3Hs4BHlFVImtYEQktWUp/\n" +
            "/gUMPdAC72eXh9C3l1x9z8QT+/oBmbiewQ3jBQ+rsoB7sEz/RSH1QK/OVjAEZZGo\n" +
            "hHmhfdVhEZxew1KdRYcKRSa66pyCVgAMJ+1UokoFwys7dt3Lx6lJB9roAwKBgCRN\n" +
            "OxQl4aOQhcRBew6XcoKdZiWdzNsBb8iAg+iadcKw3hszDp4X+q+z9i+WcJcEugxM\n" +
            "lEM5bwvzkmOlZkMRfH6PVKozebt4FawNk0GgNiaB1ssMA8WTUTqsux5P3GMMbXq/\n" +
            "ktXrPLFpQ3SLOtNS2APuxB/qTNeJeCbUzq80DorhAoGBALAU7AWjjxgjCJlVKcfc\n" +
            "iFeFh0W3HnNMQfn/ukha2Mg4Nl4GdLN18wx5VGpSCMEZ415cCy5S0SXxuUE5l6G1\n" +
            "Pb2gYF9JyGhXJLMF2x9k4he4j/qHu9RCvixYlbb/jN59aifJxTeOG53g0Fo+H9U1\n" +
            "432A3bs1MPT1Ew7zgnxjznt8\n" +
            "-----END PRIVATE KEY-----\n";
    private static final String CLUSTER_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDhjCCAm6gAwIBAgIJANzx2pPcYgmlMA0GCSqGSIb3DQEBCwUAMFcxCzAJBgNV\n" +
            "BAYTAlhYMRUwEwYDVQQHDAxEZWZhdWx0IENpdHkxHDAaBgNVBAoME0RlZmF1bHQg\n" +
            "Q29tcGFueSBMdGQxEzARBgNVBAMMCmNsdXN0ZXItY2EwIBcNMTgwODIzMTYxOTU0\n" +
            "WhgPMjExODA3MzAxNjE5NTRaMFcxCzAJBgNVBAYTAlhYMRUwEwYDVQQHDAxEZWZh\n" +
            "dWx0IENpdHkxHDAaBgNVBAoME0RlZmF1bHQgQ29tcGFueSBMdGQxEzARBgNVBAMM\n" +
            "CmNsdXN0ZXItY2EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDbFnJj\n" +
            "90sKoM35VszJsfwNvO5dshoeFIb2idf7h+l0h3GMv29j+1XtmLJGzxiYy320KFZr\n" +
            "3IKWbq+DabqdlqEqZm9NZ1Kq9d7mB10zulQce5JwVZ3FqpCmLku2jHCaDXzTKC3T\n" +
            "/Xp0O9Oe8+42ysSMCTd8p8aZ4vAyJMCKcoyVCGHrUWVba40D7cQNOlhJplSzHZdL\n" +
            "FYZ13kwzpT5GpDEPhGVmtF8qV918lSxvdpuepyeFdOSYY88FEMMLLrlZG4QCPyES\n" +
            "4FpcUXMzzvZeLIlZnKNIYbao3Kx+yZv//wjC80/pqdyoZ5+K5hDxjby2+f+2dh0T\n" +
            "adKRZC2pp+j3/z63AgMBAAGjUzBRMB0GA1UdDgQWBBThuvddCb/5TPSKYNOHkCTL\n" +
            "VghhRzAfBgNVHSMEGDAWgBThuvddCb/5TPSKYNOHkCTLVghhRzAPBgNVHRMBAf8E\n" +
            "BTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBA6oTI27dJgbVtyWxQWznKrkznZ9+t\n" +
            "mQQGbpfl9zEg7/0X7fFb+m84QHro+aNnQ4kTgZ6QBvusIpwfx1F6lQrraVrPr142\n" +
            "4DqGmY9xReNu/fj+C+8lTI5PA+mE7tMrLpQvKxI+AMttvlz8eo1SITUA+kJEiWZX\n" +
            "mjvyHXmhic4K8SnnB0gnFzHN4y09wLqRMNCRH+aI+sa9Wu8cqvpTqlelVcYV83zu\n" +
            "ydx4VZkC+zTzjI418znN/NU2CMpxLZNl0/zCrspID7v34NRmJ1AHFcrn7/XhsSvz\n" +
            "D0z+vgrfionoRhyWUDh7POlWwdUOWiBDBOFrkgeKNphSC0glYFN+2IW7\n" +
            "-----END CERTIFICATE-----\n";
    private static final String CLIENTS_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCWws5FEJOpfZ/s\n" +
            "FJWYbYdJVxmZB+5PjCwA2TUZxF/3P4w/5g2KZaXNy89AfBC5vRDRgyDyj/RwcDg8\n" +
            "0kDGKobcGhTx5YkWoNvR/2WuTN6KC8DM78bfEREDHDxiXfAXrMIi7Ux2FvUX13l7\n" +
            "6Sp9kiG3ETLjFom3n/qhg1ITJqPJSJi3tey0o2Pd5Arv0MIhQyep++URtZfND5fg\n" +
            "F5x7hgnSf9Q1P1dJnVadu+ohUmmG7g+zX4rTqjN2jmHcf9V4lLKdPGWwLQEGnP9y\n" +
            "Dqlm8x12M/BcIJasRgcciVsKYFuXe09NEYBvUjW8L6gaQ6U9wcYZ2MlKW/8LMGkS\n" +
            "FfO4quAJAgMBAAECggEAQ1NdsEQV3UQHrfMHV1naZ6so+EktaILNh9d4OjiTLqRH\n" +
            "aqW++EYqhDv3IvIEuh2vrBCmHwygebHzu12dpaGKNjLDlb8OuHc/k4k9jFgxrW5Q\n" +
            "PHT719QUR9JNORSASuJQlC5qzfW0oGAOlYJsAkXHHqzkj7sZ51HfKE+v0HOaAyHj\n" +
            "8gOeBNk1Mtb3Sj5mXpWFQGpXXuG01Vsjj7Nj/91a4KtWAWOqeagc2Bk+C0aZ7d1p\n" +
            "SQcLVWjJYwoejgCc2elZxzbfmDtVSAgFtdTPxwf9uflMducTfp/RyaQbzuYSrSmz\n" +
            "rnZq/59i9lYl314rjjkCusDaDSPdK5QziN54tQ+BcQKBgQDE9ulPecHtZhOsI9zT\n" +
            "J+xTJtZq1w8kFV5jMqXnL3jAFBXsC3s02KLq36ppvf8kVzUHrHE+DiWnHKEIiy/U\n" +
            "luMnPvJb/6qqdQNDpcrF+CE2JevvoPl5hrKdyzAI4TNu96aU+9qVrO2rB7bWBvlA\n" +
            "dVwIZ8zkk3pwbdEj9rYpMA1VVQKBgQDD8rJAEd9fLtX53NQh8XWEJ1dEfncmg/ib\n" +
            "0vyoYlqSDjPTot85sCunVZNHwUoKUsukzi+Tc9hxaXCjEB6ICVeXqWc4PYnbK79H\n" +
            "N+2X6YaO/rKAzbxM1F/Km3IzzvoXFJnPG4hxvBmpdApKgBGOVixnjD7PzNz4jh9u\n" +
            "1qhDocdf5QKBgQCDsLqporTgr0Ez9P5uR+Egb3UpFgVPkOH83R5Dhl/rvQIzQjHs\n" +
            "UXQMKeNcs+XlPFF+gfNtFDRkmSWp+rXOI9xYnyOYE0belUHLdwwudQpvk8c9/pkO\n" +
            "gdrm2bWSGlAzP22nawTo0ihOE+hRDXSVfmI8VHqP0XMpvKL6srd0rmYbyQKBgAYD\n" +
            "PXr/0WXfTwuSviOogB2lA2WDp+5ToF5PtBcKpZLTwr1cwxLHGB/TXWiXQslcTwlo\n" +
            "lkclB+A7BwzJ4tXzy29I8HTmVoOWLRFnYvAFZ26d3CZdqciFv8a8zF1QnZX1uN6F\n" +
            "DsPGrNbpS6OLmH5QoJ4wzICd3a321noVNiaVIUQNAoGAYu4RrGcBKRuy75lfKARD\n" +
            "gNxxVlvuI33ieK/3A9nUWc3LXl5D/yiSePCUs4giOwi2gFrGjcmIqLXZE5XUYGEu\n" +
            "zXWWQCGbMqyX15/A2/eTuj658F292nkSyU/5U2999WjCm79sfnGJB1zavfv2fzGK\n" +
            "g4trXCUkjAVG3Toaq05saGM=\n" +
            "-----END PRIVATE KEY-----\n";
    private static final String CLIENTS_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDhjCCAm6gAwIBAgIJAOKzFJgrn+rZMA0GCSqGSIb3DQEBCwUAMFcxCzAJBgNV\n" +
            "BAYTAlhYMRUwEwYDVQQHDAxEZWZhdWx0IENpdHkxHDAaBgNVBAoME0RlZmF1bHQg\n" +
            "Q29tcGFueSBMdGQxEzARBgNVBAMMCmNsaWVudHMtY2EwIBcNMTgwODIzMTYyMTI1\n" +
            "WhgPMjExODA3MzAxNjIxMjVaMFcxCzAJBgNVBAYTAlhYMRUwEwYDVQQHDAxEZWZh\n" +
            "dWx0IENpdHkxHDAaBgNVBAoME0RlZmF1bHQgQ29tcGFueSBMdGQxEzARBgNVBAMM\n" +
            "CmNsaWVudHMtY2EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCWws5F\n" +
            "EJOpfZ/sFJWYbYdJVxmZB+5PjCwA2TUZxF/3P4w/5g2KZaXNy89AfBC5vRDRgyDy\n" +
            "j/RwcDg80kDGKobcGhTx5YkWoNvR/2WuTN6KC8DM78bfEREDHDxiXfAXrMIi7Ux2\n" +
            "FvUX13l76Sp9kiG3ETLjFom3n/qhg1ITJqPJSJi3tey0o2Pd5Arv0MIhQyep++UR\n" +
            "tZfND5fgF5x7hgnSf9Q1P1dJnVadu+ohUmmG7g+zX4rTqjN2jmHcf9V4lLKdPGWw\n" +
            "LQEGnP9yDqlm8x12M/BcIJasRgcciVsKYFuXe09NEYBvUjW8L6gaQ6U9wcYZ2MlK\n" +
            "W/8LMGkSFfO4quAJAgMBAAGjUzBRMB0GA1UdDgQWBBQUwNmfsNj+PM240pVPxYx9\n" +
            "Q9eQhDAfBgNVHSMEGDAWgBQUwNmfsNj+PM240pVPxYx9Q9eQhDAPBgNVHRMBAf8E\n" +
            "BTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQAnhbNKwMmnayHsT6kKgyyDV6RUUYs6\n" +
            "nYf3nx+GIQWSw4c5TOHDcTWdKpOxVnLNXYKQoSkb1RBoSMLBdQwidZ5K2DB5eXaG\n" +
            "rcfEbKNBc5ZCFgFEAyy35pitJOmU/KzCdKyvx+TR5hIgGoKajYX5JZxj+1rTPGKO\n" +
            "ePT9iFp1ZbzHjgw6vFeJ+D2ov6HfW6C/KuK9Y6xUpvRQLVjMJYCyzxkxQAxZvu/0\n" +
            "0HVYYH6UJ7kuWywFMWoBdZ8US/vuUSBYyCGNL9p6ol+h9rsz3cIWBVBjx8C3qKki\n" +
            "QtlIdmFljGSaGGY6aJjUvUdgoPp1yQPa5oS+afr5g9gaEp4lxP6mc+Li\n" +
            "-----END CERTIFICATE-----\n";

    private static final String END_ENTITY_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIESjCCAzICFAom42BJSu8N8fTix+wYFeTnFXkrMA0GCSqGSIb3DQEBCwUAMFcx\n" +
            "CzAJBgNVBAYTAlhYMRUwEwYDVQQHDAxEZWZhdWx0IENpdHkxHDAaBgNVBAoME0Rl\n" +
            "ZmF1bHQgQ29tcGFueSBMdGQxEzARBgNVBAMMCmNsdXN0ZXItY2EwIBcNMjMwMTEy\n" +
            "MTE1NTEyWhgPMjEyMjEyMTkxMTU1MTJaMGoxCzAJBgNVBAYTAkRFMQwwCgYDVQQI\n" +
            "DANOUlcxEzARBgNVBAcMCkR1c3NlbGRvcmYxEDAOBgNVBAoMB1N0cmltemkxEDAO\n" +
            "BgNVBAsMB1N0cmltemkxFDASBgNVBAMMC1N0cmltemlUZXN0MIICIjANBgkqhkiG\n" +
            "9w0BAQEFAAOCAg8AMIICCgKCAgEAtLNAOsVmLa05JnCq6n8aUf5IqqWjR0Lw4Gn2\n" +
            "gqxIbe7+q88jmqPAF/aUIBmUCOcmS+GC+EaAi522mociYUcveonfoOUaSh8T23a3\n" +
            "LY4ACq2NZsUH96qUTSieFFELVSz/XXwOuAI+HCZTvG/veRFCPzL2HAJdqmTJyh9v\n" +
            "tknedKdQvhhyQYpERPtFv0DhzMw2/PUgDs3Zf1OWByv2X3xGBydCHL9ahoJzaArZ\n" +
            "A+jIXTAQl1y4T0uFusLxWjiAQ96xJnA/6d+9Rt0Urxj7H9RpChVw1vJ+qekEvrNT\n" +
            "1HrVsE26LkQzE8kL5tHAYUeFs+KIN5VKxq+cN5DLJ6eZ6Us7N0hGZrd9gg8ntlSb\n" +
            "/gsCA+SNPyVB+ix9NKvhlduPKS6N/EBsJBbiVeRzGTDuiNTHgjv/58/bdZGZ3R+9\n" +
            "KUMB4xUcO2CMvQWgd1ZDoi4PM91PERpqAj4UDFU07F3k0K8nunjk3OW3Q75eE+6L\n" +
            "3q6+Gssz8aGLzP1muZZTtZ/ZCeO46FmQ42Yt8uMS8/UMHleRAENTY9sEyFrfN3As\n" +
            "zXrXK4SdP/9dRgqjm8JalcqwkiJDR8+LTpYSy+uTY7dD3C2H1mn0eqLZp3RamvpS\n" +
            "zQcIvjQPGZWpLvvsy7V8P0jW9yA7+l5c/bqzy6B/IUAfayce7kBdICIZe9T1F4JP\n" +
            "eOfB03kCAwEAATANBgkqhkiG9w0BAQsFAAOCAQEATtZnkLygVsQX39oS+ra/eI0U\n" +
            "w1Nr03RSn/CYPEe0B9UI7FxprcbzBnQyVhzgUcr7e6aOKfAM92k1uCcFwg1YOZcB\n" +
            "u2YNGDum7AjwyazRKCXX30QY3eKyBuWCsA96z2Z8gxvmqtxKmk7X0LsoNQ4qWvxV\n" +
            "yPlKo9idFuhf2IhFwd1ucX3S+ZWRXxzQGeLjdpqf0vBUA9uqP5bjMZHwums1s/MB\n" +
            "MyngL6wecFymyjqK3kduVNKvIo/juPq0NV8u70gdHltsaJArw4sfMw/4LnR++hRD\n" +
            "EgqE8p9hEapdshRN9+YQchN44URq4xwcE/fDebru+IxPbkDOCMsj5x1WcUCg7w==\n" +
            "-----END CERTIFICATE-----";
    private static final String END_ENTITY_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIJQQIBADANBgkqhkiG9w0BAQEFAASCCSswggknAgEAAoICAQC0s0A6xWYtrTkm\n" +
            "cKrqfxpR/kiqpaNHQvDgafaCrEht7v6rzyOao8AX9pQgGZQI5yZL4YL4RoCLnbaa\n" +
            "hyJhRy96id+g5RpKHxPbdrctjgAKrY1mxQf3qpRNKJ4UUQtVLP9dfA64Aj4cJlO8\n" +
            "b+95EUI/MvYcAl2qZMnKH2+2Sd50p1C+GHJBikRE+0W/QOHMzDb89SAOzdl/U5YH\n" +
            "K/ZffEYHJ0Icv1qGgnNoCtkD6MhdMBCXXLhPS4W6wvFaOIBD3rEmcD/p371G3RSv\n" +
            "GPsf1GkKFXDW8n6p6QS+s1PUetWwTbouRDMTyQvm0cBhR4Wz4og3lUrGr5w3kMsn\n" +
            "p5npSzs3SEZmt32CDye2VJv+CwID5I0/JUH6LH00q+GV248pLo38QGwkFuJV5HMZ\n" +
            "MO6I1MeCO//nz9t1kZndH70pQwHjFRw7YIy9BaB3VkOiLg8z3U8RGmoCPhQMVTTs\n" +
            "XeTQrye6eOTc5bdDvl4T7overr4ayzPxoYvM/Wa5llO1n9kJ47joWZDjZi3y4xLz\n" +
            "9QweV5EAQ1Nj2wTIWt83cCzNetcrhJ0//11GCqObwlqVyrCSIkNHz4tOlhLL65Nj\n" +
            "t0PcLYfWafR6otmndFqa+lLNBwi+NA8Zlaku++zLtXw/SNb3IDv6Xlz9urPLoH8h\n" +
            "QB9rJx7uQF0gIhl71PUXgk9458HTeQIDAQABAoICADgpcjASBET0DswsvmJtqK+N\n" +
            "OeaX3pyaaKVHKc/JXiWU32Bk2+sHM//+qmEjsgfmV9fDumIR/4flN8jlcUEMz+vl\n" +
            "CDVIn5gj+pb+WcZ12Pt4n3cui+BlCvzEQAWOftg1SRU0Jpr4T3eOTf5GSAa334An\n" +
            "BakE7zmzY6hHhwAAC3z0N7ste+103O0Xr8DWmJd/bSPHx/Px9MSHJR0Lg+J/jIBS\n" +
            "qlCnBKrDxryyimqVohichLuWnM5Aacr3Je5lmy/8+dA5mRPGb1yj//a/6+UjrpXs\n" +
            "vgqAie+jNc9Tix2CJAJM1i3lEn72wJU34fQaN6sGIFIuO3RvRj1a6llj1QlWUYvC\n" +
            "W5Ns9azlOa+zCZ07jjGxM+QWxBx49DO2Qu53aQqqBUGugtohn5hKa+d/Y//57UA4\n" +
            "g2xS0uYLuJVgtAX4BOy8iSIxSG99+ecn8/9vjpShGtKNza1KdwjO1MkozIpYTF1w\n" +
            "ZpCFuIsvogKtwS1hfo3KWreWOBXys2IWR59oTtmkN6PoaRUUYOhtq+/B/RFKW1XQ\n" +
            "W5B1OsHRNvLgcen0HKbp3aHDRmK8x7tt2d85TkVRvLj5DpHVtry1M4LPtO9d+Zq0\n" +
            "2PGKty6NfbbHTjLQvrJ1O17xJiQ0UJxMVHzTqWpM67MhpilaWThlKMMhz0kR9KTk\n" +
            "6NWRJVCVj5byn4Kwei6lAoIBAQDTxsHuAjAnH8/qL+QDi9lBrbTKh3cLxTwnTD2w\n" +
            "8+JiuIL7LQRHZOXe4WB4xfQQpdzDZVr6hf59nhdr8MIWCLusLYOv1lKb1Goi2lKE\n" +
            "lsqHcFTo9/OcSllnlTJ9Hq0EN1zfaHCAT0PRJY8GOCAKjaffRWx3ORUBM/YePrfh\n" +
            "ooNWIsa/f+s5TzS/LJ3FYtksi3/BFC5zA/u7a0uy1g2Nq0K0BKhh7wK3LTbhhEHN\n" +
            "hZA7mGqUc6oXBDpzANiNFiMksLGo4/VZHqInuhiwNtXt3LreS1ZUkSxxEMyQSgTI\n" +
            "Y43Nzw5xTZcq+guCydzvQM400y8cpMGHAXX7/9jQ2H994rsLAoIBAQDabzUTKtXl\n" +
            "4xBYr0mVs8KBHtenE85CWuIHGF7V92mYuGhLPB/+OMD/eKL5Cp02fGLml3+FRast\n" +
            "fSWxq12V7lLDnPPpViv+rI3gAeVUKGNOgxSDln2unaglfovfEYid7AhtqTRgXZgv\n" +
            "6tTrS4YAQGTvN9sbXwVRyHPXekgVlVPaOSnO4+TdZuu0d1rTIw0/Rf/aUcyXOz2P\n" +
            "cahOhA2CVbxeEYo38Pik6RnDjLpt731R4rIGP8lXrH5xkbn9pyevdNCKxlBHrD/y\n" +
            "Pw1n3u7UR5W97nEddk3YSAMOAr6D8RSbz4r/DdRgD2FZAKJbHrMM/oE/Rd41zxF8\n" +
            "tLBLNGwSmZ4LAoIBAC+Oeaw2B5Qxm6IOYRi+xenu1SOJ6hzVjN2STGQ5UEQ1BQzc\n" +
            "nhJeQRSc7eoRIe6/IGUslJKflnelEcNmjF8gVOykR+crrN9bgv4SouctaYuimR67\n" +
            "15PoSk1tfqoEQnwo5o0wydq2chc8ZPLTlbZo+yKzV1kqk2Hyxjkigm6D7RRhuNn2\n" +
            "It96vvCTV1alDPno1aaJHqkrYtNCk/wz/1Up+U+toBZl8ukpmSJpbdF2Rd5sKrrt\n" +
            "gmuqwmli7j44k2nA2BSCJG1/6JAdRUAFAGNq5vfWWSuiciVtzVI1nP9XA9gMwESH\n" +
            "VQQMpJsZM6jyl5vbNMAs61yi4ljVql2z0GV3jeMCggEAbfZN4bhOtcv5Dqwvfw1f\n" +
            "fWDpb1KpIv5divTZyR0kK52p4zYBZRltDy7L3FNbkXJM14isyYqpAd1efHKoSjIP\n" +
            "uCnrICwhObPkOEC8EgHC/GNAkH3SB3WWkEmEYGeTPuzz0UC8/UYgtv6g8VKzwqyo\n" +
            "I0UbKExNgT5IEtGcOEFUVScxxNU1AcAuKEttjZy3roKuqllDhV5tPykYcW5I3rQK\n" +
            "f9CUpFTK1zoBnk/aCj3l+LMGq96wnVJY1RNnbioX8Fv+H951y58LEghr1z6DPJpM\n" +
            "57CBgTNtPNQDtanr/r/+f/GbJ4ruvuz/NK79DKIHwSLeLdweYTg8tWrA1RsuzK5I\n" +
            "wQKCAQAVbOYKESqbdogwT4hVXbGoGCDfxWtLkvtIoI2iAprZMwnTmBM9LJxbyOtA\n" +
            "CFlApON7E+kbTNT130twmXYo6muF7ozUwyUnuaOZzlqJosd36HFngjAjPsc9MI5l\n" +
            "OpqgIZnwOUZjqa32JDwqX8lc25vZs28B56ve/WQwO/BBuANPzbkmUUerzgHekw3z\n" +
            "h6q/b23hDaBnL1W0wxZxNmsYDoJ1HaCFb8S6fiyovhBuFc6x1m1pTt2JGetgYoVQ\n" +
            "NbnXBwcJ0kU4ABEe881ID4mAGWg7ePhBP8LoJeuzdoCDTn4X412DF6usWH3MsFCD\n" +
            "UzKvfqO8LHHn+gkP6Z7GXxmD1/Eu\n" +
            "-----END PRIVATE KEY-----";
    private static final String CERT_STORE_PASSWORD = "123456";

    private static final byte[] CLUSTER_CERT_STORE;
    private static final byte[] CLIENTS_CERT_STORE;
    private static final byte[] END_ENTITY_CERT_STORE;


    static {
        InputStream is = MockCertManager.class.getClassLoader().getResourceAsStream("CLUSTER_CERT.str");
        CLUSTER_CERT_STORE = loadResource(is);
        is = MockCertManager.class.getClassLoader().getResourceAsStream("CLIENTS_CERT.str");
        CLIENTS_CERT_STORE = loadResource(is);
        is = MockCertManager.class.getClassLoader().getResourceAsStream("END_ENTITY_CERT.str");
        END_ENTITY_CERT_STORE = loadResource(is);
    }

    public static String clusterCaCert() {
        return Base64.getEncoder().encodeToString(CLUSTER_CERT.getBytes(Charset.defaultCharset()));
    }

    public static String clusterCaKey() {
        return Base64.getEncoder().encodeToString(CLUSTER_KEY.getBytes(Charset.defaultCharset()));
    }

    public static String clientsCaCert() {
        return Base64.getEncoder().encodeToString(CLIENTS_CERT.getBytes(Charset.defaultCharset()));
    }

    public static String clientsCaKey() {
        return Base64.getEncoder().encodeToString(CLIENTS_KEY.getBytes(Charset.defaultCharset()));
    }

    public static String serverCert() {
        return Base64.getEncoder().encodeToString(END_ENTITY_CERT.getBytes(Charset.defaultCharset()));
    }

    public static String serverKey() {
        return Base64.getEncoder().encodeToString(END_ENTITY_KEY.getBytes(Charset.defaultCharset()));
    }

    public static String userCert() {
        return Base64.getEncoder().encodeToString(END_ENTITY_CERT.getBytes(Charset.defaultCharset()));
    }

    public static String userKey() {
        return Base64.getEncoder().encodeToString(END_ENTITY_KEY.getBytes(Charset.defaultCharset()));
    }

    public static String clusterCaCertStore() {
        return Base64.getEncoder().encodeToString(CLUSTER_CERT_STORE);
    }

    public static String clientsCaCertStore() {
        return Base64.getEncoder().encodeToString(CLIENTS_CERT_STORE);
    }

    public static String serverCertStore() {
        return Base64.getEncoder().encodeToString(END_ENTITY_CERT_STORE);
    }


    public static String certStorePassword() {
        return Base64.getEncoder().encodeToString(CERT_STORE_PASSWORD.getBytes(Charset.defaultCharset()));
    }


    private void write(File keyFile, String str) throws IOException {
        try (FileWriter writer = new FileWriter(keyFile)) {
            writer.write(str);
        }
    }

    private static byte[] loadResource(InputStream is) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            int read;
            byte[] data = new byte[2048];
            while ((read = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, read);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buffer.toByteArray();
    }

    /**
     * Generate a self-signed certificate
     *
     * @param keyFile  path to the file which will contain the private key
     * @param certFile path to the file which will contain the self signed certificate
     * @param sbj      subject information
     * @param days     certificate duration
     * @throws IOException
     */
    @Override
    public void generateSelfSignedCert(File keyFile, File certFile, Subject sbj, int days) throws IOException {

        write(keyFile, CLUSTER_KEY);
        write(certFile, CLUSTER_CERT);
    }

    /**
     * Renew a new self-signed certificate, keeping the existing private key
     *
     * @param keyFile  path to the file containing the existing private key
     * @param certFile path to the file which will contain the new self signed certificate
     * @param sbj      subject information
     * @param days     certificate duration
     * @throws IOException
     */
    @Override
    public void renewSelfSignedCert(File keyFile, File certFile, Subject sbj, int days) throws IOException {
        generateSelfSignedCert(keyFile, certFile, sbj, days);
    }

    @Override
    public void generateRootCaCert(Subject subject, File subjectKeyFile, File subjectCertFile, ZonedDateTime notBefore, ZonedDateTime notAfter, int pathLength) throws IOException {
        generateSelfSignedCert(subjectKeyFile, subjectCertFile, subject, 1);
    }

    @Override
    public void generateIntermediateCaCert(File issuerCaKeyFile, File issuerCaCertFile, Subject subject, File subjectKeyFile, File subjectCertFile, ZonedDateTime notBefore, ZonedDateTime notAfter, int pathLength) throws IOException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void addCertToTrustStore(File certFile, String certAlias, File trustStoreFile, String trustStorePassword)
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        if (certFile.getName().contains("cluster")) {
            Files.write(trustStoreFile.toPath(), CLUSTER_CERT_STORE);
        } else if (certFile.getName().contains("clients")) {
            Files.write(trustStoreFile.toPath(), CLIENTS_CERT_STORE);
        }
    }

    @Override
    public void addKeyAndCertToKeyStore(File keyFile, File certFile, String alias, File keyStoreFile, String keyStorePassword) throws IOException {
        Files.write(keyStoreFile.toPath(), END_ENTITY_CERT_STORE);
    }

    @Override
    public void deleteFromTrustStore(List<String> aliases, File trustStoreFile, String trustStorePassword)
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        // never called during the tests which use this MockCertManager
    }

    /**
     * Generate a certificate sign request
     *
     * @param keyFile path to the file which will contain the private key
     * @param csrFile path to the file which will contain the certificate sign request
     * @param sbj     subject information
     */
    @Override
    public void generateCsr(File keyFile, File csrFile, Subject sbj) throws IOException {
        write(keyFile, END_ENTITY_KEY);
        write(csrFile, END_ENTITY_CERT);
    }

    @Override
    public void generateCert(File csrFile, File caKey, File caCert, File crtFile, Subject sbj, int days) throws IOException {
//        write(crtFile, END_ENTITY_CERT);
        write(caCert, "cert");



    }

    @Override
    public void generateCert(File csrFile, byte[] caKey, byte[] caCert, File crtFile, Subject sbj, int days) throws IOException {
//        write(crtFile, END_ENTITY_CERT);
        write(crtFile, "cert");
    }
}
