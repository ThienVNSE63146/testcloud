/*
START_POINT_ARRcheckQuestion1:5-checkQuestion2:5END_POINT_ARR
*/

#include <stdio.h>
#include <stdlib.h>
#include <CUnit/Basic.h>
#include <CUnit/TestRun.h>
#include <CUnit/Automated.h>

#include "student/TemplateQuestion.c"

/*
 * 
 */
int init_suite(void) {
    return 0;
}

int clean_suite(void) {
    return 0;
}

void checkQuestion1() {
    CU_ASSERT(2 == question1(1,1));
}

void checkQuestion2() {
    CU_ASSERT(2 == question2(1,2));
}

int main() {
    CU_pSuite pSuite = NULL;
    /* Initialize the CUnit test registry */
    if (CUE_SUCCESS != CU_initialize_registry())
        return CU_get_error();

    /* Add a suite to the registry */
    pSuite = CU_add_suite("Dummy", init_suite, clean_suite);
    if (NULL == pSuite) {
        CU_cleanup_registry();
        return CU_get_error();
    }

    /* Add the tests to the suite */
    if ((NULL == CU_add_test(pSuite, "checkQuestion1", checkQuestion1)) ||
            (NULL == CU_add_test(pSuite, "checkQuestion2", checkQuestion2))) {
        CU_cleanup_registry();
        return CU_get_error();
    }

    /* Run all tests using the CUnit Basic interface */
    CU_basic_set_mode(CU_BRM_VERBOSE);
    CU_basic_run_tests();
    CU_automated_run_tests();
    CU_list_tests_to_file();

    CU_cleanup_registry();

    return CU_get_error();
}


